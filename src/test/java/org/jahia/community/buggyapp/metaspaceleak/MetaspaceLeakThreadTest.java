package org.jahia.community.buggyapp.metaspaceleak;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MetaspaceLeakThreadTest {

    private static final long RUN_MS = 200L;
    private static final long TIMEOUT_MS = 5_000L;

    @Test
    void canRunSeveralTimesInTheSameJvm() throws InterruptedException {
        // First run: generates MetaspaceObject classes
        final MetaspaceLeakThread first = runOnce();
        assertNull(first.getFailure());
        assertTrue(first.getClassesCreated() > 0, "first run should create classes");

        // Second run used to die immediately with "frozen class (cannot edit)"
        final MetaspaceLeakThread second = runOnce();
        assertNull(second.getFailure(), () -> "second run failed: " + second.getFailure());
        assertTrue(second.getClassesCreated() > 0, "second run should create classes too");
    }

    @Test
    void generatedClassesAreDefinedInTheModuleClassLoader() throws Exception {
        final MetaspaceLeakThread thread = runOnce();

        final Class<?> generated = Class.forName(thread.getClassName(0), false,
                MetaspaceLeakThread.class.getClassLoader());

        assertNotNull(generated);
    }

    private static MetaspaceLeakThread runOnce() throws InterruptedException {
        final MetaspaceLeakThread thread = new MetaspaceLeakThread();
        thread.start();
        Thread.sleep(RUN_MS);
        thread.doStop();
        thread.join(TIMEOUT_MS);
        return thread;
    }
}
