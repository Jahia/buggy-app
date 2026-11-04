package org.jahia.community.buggyapp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class AbstractBuggyActionTest {

    private static final long JOIN_TIMEOUT_MS = 5_000L;

    /** Minimal action whose production mode is controlled by the test. */
    private static final class RecordingAction extends AbstractBuggyAction {

        private final boolean productionMode;
        private final AtomicBoolean executed = new AtomicBoolean();

        RecordingAction(boolean productionMode) {
            this.productionMode = productionMode;
        }

        @Override
        protected Object doExecute() {
            executed.set(true);
            return "done";
        }

        @Override
        protected boolean isProductionMode() {
            return productionMode;
        }
    }

    /** Busy worker that only stops through {@link StoppableThread#doStop()}. */
    private static final class SpinningWorker extends StoppableThread {

        SpinningWorker(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (keepRunning()) {
                Thread.onSpinWait();
            }
        }
    }

    @Test
    void refusesToRunInProductionModeWithoutForce() throws Exception {
        final RecordingAction action = new RecordingAction(true);

        final Object result = action.execute();

        assertFalse(action.executed.get());
        assertTrue(String.valueOf(result).contains("--force"));
    }

    @Test
    void runsInProductionModeWhenForced() throws Exception {
        final RecordingAction action = new RecordingAction(true);
        action.setForce(true);

        assertEquals("done", action.execute());
        assertTrue(action.executed.get());
    }

    @Test
    void runsOutsideProductionMode() throws Exception {
        final RecordingAction action = new RecordingAction(false);

        assertEquals("done", action.execute());
    }

    @Test
    void defaultProductionModeCheckToleratesMissingJahiaSettings() {
        // Outside Jahia (unit tests) SettingsBean is not initialised: the guard must not blow up
        final AbstractBuggyAction action = new AbstractBuggyAction() {
            @Override
            protected Object doExecute() {
                return null;
            }
        };

        assertFalse(action.isProductionMode());
    }

    @Test
    void requirePositiveRejectsZeroAndNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> AbstractBuggyAction.requirePositive(0L, "duration"));
        assertThrows(IllegalArgumentException.class, () -> AbstractBuggyAction.requirePositive(-5L, "duration"));
        assertEquals(3L, AbstractBuggyAction.requirePositive(3L, "duration"));
    }

    @Test
    void runForStopsWorkersAfterDuration() throws InterruptedException {
        final List<SpinningWorker> workers = List.of(new SpinningWorker("w1"), new SpinningWorker("w2"));

        AbstractBuggyAction.runFor(50L, workers);

        for (SpinningWorker worker : workers) {
            worker.join(JOIN_TIMEOUT_MS);
            assertFalse(worker.isAlive(), worker.getName() + " should be stopped");
        }
    }

    @Test
    void runForStopsWorkersWhenTheShellThreadIsInterrupted() throws InterruptedException {
        // Arrange: a long run, as when a trainee presses Ctrl-C in the Karaf shell
        final List<SpinningWorker> workers = List.of(new SpinningWorker("w1"), new SpinningWorker("w2"));
        final AtomicBoolean interrupted = new AtomicBoolean();
        final Thread shell = new Thread(() -> {
            try {
                AbstractBuggyAction.runFor(60_000L, workers);
            } catch (InterruptedException ex) {
                interrupted.set(true);
            }
        });

        // Act
        shell.start();
        waitUntilAllAlive(workers);
        shell.interrupt();
        shell.join(JOIN_TIMEOUT_MS);

        // Assert
        assertTrue(interrupted.get(), "the interruption must still be propagated to the caller");
        for (SpinningWorker worker : workers) {
            worker.join(JOIN_TIMEOUT_MS);
            assertFalse(worker.isAlive(), worker.getName() + " must not keep running after Ctrl-C");
        }
    }

    private static void waitUntilAllAlive(List<? extends Thread> threads) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + JOIN_TIMEOUT_MS;
        while (!threads.stream().allMatch(Thread::isAlive) && System.currentTimeMillis() < deadline) {
            Thread.sleep(5L);
        }
    }
}
