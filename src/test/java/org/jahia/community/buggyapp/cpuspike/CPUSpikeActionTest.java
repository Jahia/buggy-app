package org.jahia.community.buggyapp.cpuspike;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CPUSpikeActionTest {

    private static final String THREAD_PREFIX = "buggy-app-cpu-spike-";
    private static final long TIMEOUT_MS = 5_000L;

    @Test
    void spikerThreadsAreGoneAfterTheDuration() throws Exception {
        final CPUSpikeAction action = new CPUSpikeAction();
        action.duration = 50L;
        action.numberOfThreads = 2;

        action.execute();

        assertTrue(spikerThreads().isEmpty(), "remaining spikers: " + spikerThreads());
    }

    @Test
    void spikerThreadsStopWhenTheCommandIsInterrupted() throws Exception {
        // Arrange
        final CPUSpikeAction action = new CPUSpikeAction();
        action.duration = 60_000L;
        action.numberOfThreads = 2;
        final Thread shell = new Thread(() -> {
            try {
                action.execute();
            } catch (Exception expected) {
                // InterruptedException is the expected outcome of Ctrl-C
            }
        });

        // Act
        shell.start();
        waitFor(() -> spikerThreads().size() == 2);
        shell.interrupt();
        shell.join(TIMEOUT_MS);

        // Assert
        assertFalse(shell.isAlive());
        waitFor(() -> spikerThreads().isEmpty());
        assertTrue(spikerThreads().isEmpty(), "CPU spike must not survive Ctrl-C: " + spikerThreads());
    }

    @Test
    void rejectsNonPositiveThreadCountBeforeStartingAnything() {
        final CPUSpikeAction action = new CPUSpikeAction();
        action.numberOfThreads = 0;

        assertThrows(IllegalArgumentException.class, action::execute);
    }

    @Test
    void rejectsNegativeDurationBeforeStartingAnything() {
        final CPUSpikeAction action = new CPUSpikeAction();
        action.duration = -1L;

        assertThrows(IllegalArgumentException.class, action::execute);
        assertTrue(spikerThreads().isEmpty());
    }

    private static Set<String> spikerThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .map(Thread::getName)
                .filter(name -> name.startsWith(THREAD_PREFIX))
                .collect(Collectors.toSet());
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(5L);
        }
    }
}
