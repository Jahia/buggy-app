package org.jahia.community.buggyapp.deadlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Command;
import org.junit.jupiter.api.Test;

class DeadLockActionTest {

    @Test
    void commandIsNamedDeadLock() {
        // Was misspelled "deak-lock", so "buggy-app:dead-lock" did not exist
        final Command command = DeadLockAction.class.getAnnotation(Command.class);

        assertEquals("buggy-app", command.scope());
        assertEquals("dead-lock", command.name());
    }

    @Test
    void deadlockedThreadsHaveRecognisableNames() {
        // Named threads make the "Found one Java-level deadlock" section of a thread dump easy to read
        assertEquals("buggy-app-deadlock-A", new ThreadA().getName());
        assertEquals("buggy-app-deadlock-B", new ThreadB().getName());
    }

    @Test
    void producesADeadlockTheJvmCanDetect() throws Exception {
        // Leaves two permanently blocked threads in the (forked) test JVM: that is the scenario
        final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        new DeadLockAction().execute();
        long[] deadlocked = null;
        final long deadline = System.currentTimeMillis() + 30_000L;
        while (deadlocked == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(200L);
            deadlocked = threads.findMonitorDeadlockedThreads();
        }

        assertNotNull(deadlocked, "no deadlock detected");
        final Set<String> names = Arrays.stream(threads.getThreadInfo(deadlocked))
                .map(ThreadInfo::getThreadName).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("buggy-app-deadlock-A", "buggy-app-deadlock-B")), names::toString);
    }
}
