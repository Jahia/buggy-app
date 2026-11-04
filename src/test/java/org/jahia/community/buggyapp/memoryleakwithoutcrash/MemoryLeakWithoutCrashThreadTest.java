package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import static org.jahia.community.buggyapp.TestWaits.letRun;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemoryLeakWithoutCrashThreadTest {

    private static final long MB = 1024L * 1024L;
    private static final float DELTA = 0.0001f;

    @Test
    void headroomCountsTheHeapThatCanStillBeCommitted() {
        // -Xms256m -Xmx1g, committed heap almost full: only 1 MB free in the 256 MB committed so far.
        // The old formula free/max = 1/1024 < 1% made the thread stop leaking at ~256 MB.
        final float ratio = MemoryLeakWithoutCrashThread.headroomRatio(1024 * MB, 256 * MB, MB);

        assertEquals((1024f - 255f) / 1024f, ratio, DELTA);
        assertTrue(ratio >= MemoryLeakWithoutCrashThread.MIN_HEADROOM_RATIO);
    }

    @Test
    void headroomIsLowWhenTheWholeHeapIsUsed() {
        final float ratio = MemoryLeakWithoutCrashThread.headroomRatio(1024 * MB, 1024 * MB, 5 * MB);

        assertTrue(ratio < MemoryLeakWithoutCrashThread.MIN_HEADROOM_RATIO);
    }

    @Test
    void batchPauseNeverDividesByZeroOnTinyHeaps() {
        assertEquals(1000L, MemoryLeakWithoutCrashThread.batchPause(1000L, 1L));
    }

    @Test
    void batchPauseSpreadsTheLeakOverTheDuration() {
        final long batches = 1024 * MB / MemoryLeakWithoutCrashThread.BYTES_PER_BATCH;

        assertEquals(60_000L / batches, MemoryLeakWithoutCrashThread.batchPause(60_000L, 1024 * MB));
    }

    @Test
    void stopsPromptlyAndReleasesTheRetainedMemory() throws InterruptedException {
        final MemoryLeakWithoutCrashThread thread = new MemoryLeakWithoutCrashThread(60_000L);

        thread.start();
        letRun(100L);
        thread.doStop();
        thread.join(5_000L);

        assertFalse(thread.isAlive(), "doStop must wake the thread up even while it sleeps");
        assertEquals(0, thread.getRetainedEntries());
    }
}
