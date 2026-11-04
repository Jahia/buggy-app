package org.jahia.community.buggyapp.deadlock;

import static org.jahia.community.buggyapp.TestWaits.waitFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
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
    void producesADeadlockTheJvmCanDetect() throws Exception {
        // Leaves two permanently blocked threads in the (forked) test JVM: that is the scenario
        final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        new DeadLockAction().execute();
        waitFor(Duration.ofSeconds(30), () -> threads.findMonitorDeadlockedThreads() != null);
        final long[] deadlocked = threads.findMonitorDeadlockedThreads();

        assertNotNull(deadlocked, "no deadlock detected");
        final Set<Long> deadlockedIds = Arrays.stream(deadlocked).boxed().collect(Collectors.toSet());
        final Set<Class<?>> deadlockedTypes = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> deadlockedIds.contains(t.getId()))
                .map(Object::getClass).collect(Collectors.toSet());
        assertTrue(deadlockedTypes.containsAll(Set.of(ThreadA.class, ThreadB.class)), deadlockedTypes::toString);
    }
}
