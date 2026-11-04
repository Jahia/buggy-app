package org.jahia.community.buggyapp.metaspaceleak;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MetaspaceLeakActionTest {

    @Test
    void runsForTheDurationAndCanBeRepeated() throws Exception {
        final MetaspaceLeakAction action = new MetaspaceLeakAction();
        action.duration = 100L;

        assertNull(action.execute());
        assertNull(action.execute());
    }

    @Test
    void rejectsNonPositiveDuration() {
        final MetaspaceLeakAction action = new MetaspaceLeakAction();
        action.duration = 0L;

        assertThrows(IllegalArgumentException.class, action::execute);
    }
}
