package org.jahia.community.buggyapp.dbconnectionleak;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Borrows connections from a pool and never gives them back while running: the leak reaches {@code target}
 * connections at half of {@code duration} and is then held. Every leaked connection is closed (returned to the
 * pool) when the thread stops, including a borrow that was still waiting at that moment.
 */
public class DbConnectionLeakThread extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnectionLeakThread.class);
    private static final long HOLD_STEP_MS = 1_000L;
    private static final long MIN_PAUSE_MS = 1L;
    private final DataSource dataSource;
    private final int target;
    private final long pauseBetweenBorrows;
    /** Leaked connections; guarded by itself. */
    private final List<Connection> leaked = new ArrayList<>();
    private volatile int failedBorrows;

    public DbConnectionLeakThread(DataSource dataSource, int target, long duration) {
        if (target <= 0) {
            throw new IllegalArgumentException("number of connections must be > 0, got " + target);
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be > 0, got " + duration);
        }
        this.dataSource = dataSource;
        this.target = target;
        this.pauseBetweenBorrows = Math.max(MIN_PAUSE_MS, duration / 2 / target);
    }

    @Override
    public void doStop() {
        super.doStop();
        // Return the connections right away: Jahia must not wait for this thread to notice the stop
        releaseAll();
        interrupt();
    }

    @Override
    public void run() {
        try {
            while (keepRunning()) {
                if (leakedCount() < target) {
                    borrowOne();
                    Thread.sleep(pauseBetweenBorrows);
                } else {
                    Thread.sleep(HOLD_STEP_MS);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            releaseAll();
        }
    }

    private void borrowOne() {
        final Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException ex) {
            failedBorrows++;
            LOGGER.warn("Cannot borrow a connection with {} already leaked: {}", leakedCount(), ex.getMessage());
            return;
        }
        if (!keepIfRunning(connection)) {
            closeQuietly(connection);
            return;
        }
        try {
            // Use the connection so that it is really taken from the pool, even behind a lazy proxy
            connection.getMetaData();
        } catch (SQLException ex) {
            LOGGER.debug("Cannot read the metadata of a leaked connection", ex);
        }
        final int count = leakedCount();
        LOGGER.info("Leaked {} of {} connections (pool: {} active / {} max)", count, target,
                describe(PoolIntrospection.active(dataSource)), describe(PoolIntrospection.maxActive(dataSource)));
    }

    private boolean keepIfRunning(Connection connection) {
        synchronized (leaked) {
            if (!keepRunning()) {
                return false;
            }
            leaked.add(connection);
            return true;
        }
    }

    private void releaseAll() {
        final List<Connection> toClose;
        synchronized (leaked) {
            toClose = new ArrayList<>(leaked);
            leaked.clear();
        }
        toClose.forEach(DbConnectionLeakThread::closeQuietly);
        if (!toClose.isEmpty()) {
            LOGGER.info("{} leaked connections returned to the pool", toClose.size());
        }
    }

    private int leakedCount() {
        synchronized (leaked) {
            return leaked.size();
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            LOGGER.error("Failed to return a leaked connection to the pool", ex);
        }
    }

    private static String describe(java.util.OptionalInt value) {
        return value.isPresent() ? String.valueOf(value.getAsInt()) : "?";
    }

    int getFailedBorrows() {
        return failedBorrows;
    }
}
