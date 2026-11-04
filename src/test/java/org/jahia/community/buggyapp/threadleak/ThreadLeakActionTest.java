package org.jahia.community.buggyapp.threadleak;

import static org.jahia.community.buggyapp.TestWaits.waitFor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ThreadLeakActionTest {

    private static final long TIMEOUT_MS = 10_000L;

    @Test
    void leaksThreadsDuringTheRunAndReleasesThemAfterwards() throws Exception {
        // Arrange
        final Set<Long> before = liveThreadIds();
        final ThreadLeakAction action = new ThreadLeakAction();
        action.duration = 1_000L;
        final AtomicReference<Exception> failure = new AtomicReference<>();
        final Thread shell = new Thread(() -> {
            try {
                action.execute();
            } catch (Exception ex) {
                failure.set(ex);
            }
        });

        // Act
        shell.start();
        waitFor(() -> newThreads(before).size() > 10);
        final int leakedDuringRun = newThreads(before).size();
        shell.join(TIMEOUT_MS);

        // Assert
        assertFalse(shell.isAlive());
        assertNull(failure.get(), () -> "action failed: " + failure.get());
        assertTrue(leakedDuringRun > 10, "the scenario should leak threads while running: " + leakedDuringRun);
        waitFor(() -> newThreads(before).isEmpty());
        assertTrue(newThreads(before).isEmpty(), () -> "threads left behind: " + newThreads(before));
    }

    @Test
    void closingBeforeTheThreadRunsDoesNotStartAnyClient() throws InterruptedException {
        final Set<Long> before = liveThreadIds();
        final ThreadLeakThread thread = new ThreadLeakThread();

        thread.close();
        thread.start();
        thread.join(TIMEOUT_MS);

        waitFor(() -> newThreads(before).isEmpty());
        assertTrue(newThreads(before).isEmpty(), () -> "client started after close: " + newThreads(before));
    }

    private static Set<Long> liveThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive).map(Thread::getId).collect(Collectors.toSet());
    }

    private static Set<String> newThreads(Set<Long> before) {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(t -> !before.contains(t.getId()))
                .filter(t -> t != Thread.currentThread())
                .map(Thread::getName)
                .collect(Collectors.toSet());
    }

}
