package org.jahia.community.buggyapp.nativememoryleak;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NativeMemoryLeakActionTest {

    @Test
    void invalidOptionsFailInTheShellNotInTheBackgroundThread() {
        final NativeMemoryLeakAction action = new NativeMemoryLeakAction();
        action.size = 10L;
        action.totalSize = 5L;

        assertThrows(IllegalArgumentException.class, action::execute);
    }

    @Test
    void startsTheLeakInBackground() throws Exception {
        final NativeMemoryLeakAction action = new NativeMemoryLeakAction();
        action.duration = 10L;
        action.size = 1024L;
        action.totalSize = 2048L;

        final Object result = action.execute();

        assertTrue(String.valueOf(result).contains("buggy-app-native-memory-leak"));
    }
}
