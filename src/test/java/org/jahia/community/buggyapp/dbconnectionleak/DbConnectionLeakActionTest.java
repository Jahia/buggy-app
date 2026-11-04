package org.jahia.community.buggyapp.dbconnectionleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DbConnectionLeakActionTest {

    private static final long TIMEOUT_MS = 10_000L;

    /** Action wired to a stub pool instead of Jahia's datasource. */
    private static DbConnectionLeakAction actionOn(DataSource pool, long duration, int connections) {
        final DbConnectionLeakAction action = new DbConnectionLeakAction() {
            @Override
            DataSource jahiaDataSource() {
                return pool;
            }
        };
        action.duration = duration;
        action.connections = connections;
        return action;
    }

    @Test
    void leaksTheRequestedConnectionsThenReturnsThemAll() throws Exception {
        // Arrange
        final StubPool pool = new StubPool(10, 50L);
        final Thread shell = runInBackground(actionOn(pool, 800L, 4));

        // Act + Assert: the leak reaches its target and holds it
        waitFor(() -> pool.getActive() == 4);
        assertEquals(4, pool.getActive());
        shell.join(TIMEOUT_MS);

        // All connections are returned to the pool at the end
        assertFalse(shell.isAlive());
        assertEquals(0, pool.getActive());
    }

    @Test
    void byDefaultExhaustsTheWholePoolSoOtherBorrowersTimeOut() throws Exception {
        final StubPool pool = new StubPool(5, 50L);
        final Thread shell = runInBackground(actionOn(pool, 1_000L, 0));

        waitFor(() -> pool.getActive() == 5);

        // What Jahia's own requests experience: "Timeout: Pool empty"
        assertThrows(SQLException.class, pool::getConnection);
        shell.join(TIMEOUT_MS);
        assertEquals(0, pool.getActive());
    }

    @Test
    void keepsTryingWithoutSpinningWhenThePoolIsSmallerThanTheTarget() throws InterruptedException {
        // Arrange: target 5 on a pool of 2 with a short borrow timeout
        final StubPool pool = new StubPool(2, 20L);
        final DbConnectionLeakThread thread = new DbConnectionLeakThread(pool, 5, 200L);

        // Act
        thread.start();
        Thread.sleep(600L);
        thread.doStop();
        thread.join(TIMEOUT_MS);

        // Assert
        assertFalse(thread.isAlive());
        assertTrue(thread.getFailedBorrows() > 0, "the exhausted pool must have refused some borrows");
        assertTrue(thread.getFailedBorrows() < 100, "failed borrows must be paced: " + thread.getFailedBorrows());
        assertEquals(0, pool.getActive());
    }

    @Test
    void stopReleasesEvenWhenABorrowIsStillWaiting() throws InterruptedException {
        // Arrange: pool of 1 with a long borrow timeout; the second borrow waits for a free connection
        final StubPool pool = new StubPool(1, 5_000L);
        final DbConnectionLeakThread thread = new DbConnectionLeakThread(pool, 3, 100L);
        thread.start();
        waitFor(() -> pool.getActive() == 1);
        Thread.sleep(100L);

        // Act
        thread.doStop();
        thread.join(TIMEOUT_MS);

        // Assert: the pending borrow got the released connection and closed it too
        assertFalse(thread.isAlive());
        waitFor(() -> pool.getActive() == 0);
        assertEquals(0, pool.getActive());
    }

    @Test
    void requiresAnExplicitTargetWhenThePoolSizeCannotBeDetected() {
        final DataSource opaque = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DataSource.class}, (proxy, method, args) -> null);

        assertThrows(IllegalArgumentException.class, () -> actionOn(opaque, 1_000L, 0).execute());
    }

    @Test
    void rejectsInvalidOptions() {
        final StubPool pool = new StubPool(2, 10L);

        assertThrows(IllegalArgumentException.class, () -> actionOn(pool, 0L, 1).execute());
        assertThrows(IllegalArgumentException.class, () -> actionOn(pool, 1_000L, -1).execute());
    }

    @Test
    void poolSizeIsFoundThroughDelegatingDataSources() {
        // Jahia's datasource is a Spring DelegatingDataSource around the Tomcat JDBC pool
        final StubPool target = new StubPool(7, 10L);
        final Object wrapper = new DelegatingStub(target);

        assertEquals(OptionalInt.of(7), PoolIntrospection.maxActive(wrapper));
        assertEquals(OptionalInt.of(0), PoolIntrospection.active(wrapper));
        assertEquals(OptionalInt.empty(), PoolIntrospection.maxActive(new Object()));
    }

    /** Mimics org.springframework.jdbc.datasource.DelegatingDataSource#getTargetDataSource(). */
    public static final class DelegatingStub {

        private final DataSource target;

        DelegatingStub(DataSource target) {
            this.target = target;
        }

        public DataSource getTargetDataSource() {
            return target;
        }
    }

    private static Thread runInBackground(DbConnectionLeakAction action) {
        final AtomicReference<Exception> failure = new AtomicReference<>();
        final Thread shell = new Thread(() -> {
            try {
                action.execute();
            } catch (Exception ex) {
                failure.set(ex);
            }
        });
        shell.start();
        return shell;
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10L);
        }
    }
}
