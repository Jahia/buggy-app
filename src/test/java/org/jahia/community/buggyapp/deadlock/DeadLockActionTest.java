package org.jahia.community.buggyapp.deadlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
