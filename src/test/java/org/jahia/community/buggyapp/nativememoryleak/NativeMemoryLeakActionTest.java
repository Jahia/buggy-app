package org.jahia.community.buggyapp.nativememoryleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class NativeMemoryLeakActionTest {

    private static final long ONE_MB = 1024L * 1024L;

    @AfterEach
    void stopAnyRunningLeak() throws Exception {
        stopAction().execute();
    }

    @Test
    void invalidOptionsFailInTheShellNotInTheBackgroundThread() {
        final NativeMemoryLeakAction action = new NativeMemoryLeakAction();
        action.size = 10L;
        action.totalSize = 5L;

        assertThrows(IllegalArgumentException.class, action::execute);
    }

    @Test
    void startsInBackgroundAndRefusesASecondConcurrentLeak() throws Exception {
        final Object first = longLeak().execute();
        final Object second = longLeak().execute();

        assertTrue(String.valueOf(first).contains("started"), String.valueOf(first));
        assertTrue(String.valueOf(second).contains("already running"), String.valueOf(second));
    }

    @Test
    void stopEndsTheLeakEarlyAndReleasesTheMemory() throws Exception {
        longLeak().execute();

        final Object result = stopAction().execute();

        assertEquals("Native memory leak stopped, memory released", result);
        assertEquals("No native memory leak is running", stopAction().execute());
    }

    private static NativeMemoryLeakAction longLeak() {
        final NativeMemoryLeakAction action = new NativeMemoryLeakAction();
        action.duration = 60_000L;
        action.size = ONE_MB;
        action.totalSize = 4 * ONE_MB;
        return action;
    }

    private static NativeMemoryLeakAction stopAction() {
        final NativeMemoryLeakAction action = new NativeMemoryLeakAction();
        action.stop = true;
        return action;
    }
}
