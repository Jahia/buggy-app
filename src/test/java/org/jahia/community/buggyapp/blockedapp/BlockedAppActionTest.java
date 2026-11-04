package org.jahia.community.buggyapp.blockedapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class BlockedAppActionTest {

    private static final long TIMEOUT_MS = 10_000L;
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    @Test
    void allButOneThreadAreBlockedOnTheMonitorHeldByTheOwner() throws Exception {
        // Arrange
        final BlockedAppAction action = new BlockedAppAction();
        action.duration = 60_000L;
        action.numberOfThreads = 4;
        final Thread shell = new Thread(() -> {
            try {
                action.execute();
            } catch (Exception expected) {
                // InterruptedException: Ctrl-C
            }
        });

        // Act
        shell.start();
        waitFor(() -> blockedOnAppObject().size() == 3);
        final List<ThreadInfo> blocked = blockedOnAppObject();

        // Assert: what a thread dump shows, "waiting to lock <0x...> (a ...AppObject) held by Thread-N"
        assertEquals(3, blocked.size(), "3 of the 4 threads must be BLOCKED");
        final Set<Long> owners = blocked.stream().map(ThreadInfo::getLockOwnerId).collect(Collectors.toSet());
        assertEquals(1, owners.size(), "they all wait for the same owner");
        final long ownerId = owners.iterator().next();
        assertTrue(appThreads().anyMatch(t -> t.getId() == ownerId), "the owner is one of the scenario threads");

        // The blocked state is recoverable, unlike the deadlock: Ctrl-C releases everything
        shell.interrupt();
        shell.join(TIMEOUT_MS);
        waitFor(() -> appThreads().findAny().isEmpty());
        assertFalse(appThreads().findAny().isPresent(), "all threads must be released");
    }

    @Test
    void releasesEveryThreadAtTheEndOfTheDuration() throws Exception {
        final BlockedAppAction action = new BlockedAppAction();
        action.duration = 200L;
        action.numberOfThreads = 3;

        action.execute();

        waitFor(() -> appThreads().findAny().isEmpty());
        assertFalse(appThreads().findAny().isPresent());
    }

    @Test
    void needsAtLeastTwoThreadsToBlockAnything() {
        final BlockedAppAction action = new BlockedAppAction();
        action.numberOfThreads = 1;

        assertThrows(IllegalArgumentException.class, action::execute);
    }

    @Test
    void rejectsNonPositiveDuration() {
        final BlockedAppAction action = new BlockedAppAction();
        action.duration = 0L;

        assertThrows(IllegalArgumentException.class, action::execute);
    }

    private static Stream<Thread> appThreads() {
        return Thread.getAllStackTraces().keySet().stream().filter(Thread::isAlive).filter(AppThread.class::isInstance);
    }

    private static List<ThreadInfo> blockedOnAppObject() {
        final long[] ids = appThreads().mapToLong(Thread::getId).toArray();
        return Stream.of(THREADS.getThreadInfo(ids))
                .filter(Objects::nonNull)
                .filter(info -> info.getThreadState() == Thread.State.BLOCKED)
                .filter(info -> String.valueOf(info.getLockName()).startsWith(AppObject.class.getName()))
                .collect(Collectors.toList());
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
    }
}
