package org.jahia.community.buggyapp.dbconnectionleak;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Bounded connection pool behaving like Jahia's Tomcat JDBC pool: borrowing from an exhausted pool waits up to
 * {@code borrowTimeoutMs} then fails; it exposes {@code getMaxActive()} / {@code getActive()} like
 * {@code org.apache.tomcat.jdbc.pool.DataSourceProxy}.
 */
public class StubPool implements DataSource {

    private final int maxActive;
    private final long borrowTimeoutMs;
    private final Semaphore permits;

    public StubPool(int maxActive, long borrowTimeoutMs) {
        this.maxActive = maxActive;
        this.borrowTimeoutMs = borrowTimeoutMs;
        this.permits = new Semaphore(maxActive);
    }

    public int getMaxActive() {
        return maxActive;
    }

    public int getActive() {
        return maxActive - permits.availablePermits();
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            if (!permits.tryAcquire(borrowTimeoutMs, TimeUnit.MILLISECONDS)) {
                throw new SQLException("Timeout: Pool empty. Unable to fetch a connection in "
                        + borrowTimeoutMs + " ms");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Pool wait interrupted", ex);
        }
        return newConnection();
    }

    private Connection newConnection() {
        final AtomicBoolean closed = new AtomicBoolean();
        final DatabaseMetaData metaData = (DatabaseMetaData) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> "getDatabaseProductName".equals(method.getName()) ? "StubDB" : null);
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "close":
                            if (closed.compareAndSet(false, true)) {
                                permits.release();
                            }
                            return null;
                        case "isClosed":
                            return closed.get();
                        case "getMetaData":
                            return metaData;
                        default:
                            return null;
                    }
                });
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        // not used
    }

    @Override
    public void setLoginTimeout(int seconds) {
        // not used
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
