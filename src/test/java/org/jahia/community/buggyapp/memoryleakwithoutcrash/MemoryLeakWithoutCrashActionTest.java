package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MemoryLeakWithoutCrashActionTest {

    @Test
    void runsForTheDurationThenReturns() throws Exception {
        final MemoryLeakWithoutCrashAction action = new MemoryLeakWithoutCrashAction();
        action.duration = 100L;

        assertNull(action.execute());
    }

    @Test
    void rejectsNonPositiveDuration() {
        final MemoryLeakWithoutCrashAction action = new MemoryLeakWithoutCrashAction();
        action.duration = -1L;

        assertThrows(IllegalArgumentException.class, action::execute);
    }
}
