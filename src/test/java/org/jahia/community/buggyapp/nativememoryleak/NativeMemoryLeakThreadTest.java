package org.jahia.community.buggyapp.nativememoryleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NativeMemoryLeakThreadTest {

    private static final long ONE_MB = 1024L * 1024L;

    @Test
    void rejectsStepLargerThanTotalInsteadOfDividingByZero() {
        assertThrows(IllegalArgumentException.class, () -> new NativeMemoryLeakThread(1000L, 2 * ONE_MB, ONE_MB));
    }

    @Test
    void rejectsNonPositiveStepSize() {
        assertThrows(IllegalArgumentException.class, () -> new NativeMemoryLeakThread(1000L, 0L, ONE_MB));
    }

    @Test
    void rejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> new NativeMemoryLeakThread(-1L, ONE_MB, ONE_MB));
    }

    @Test
    void growsToTotalSizeThenFreesEverything() throws InterruptedException {
        // Arrange
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(30L, ONE_MB, 3 * ONE_MB);

        // Act
        thread.start();
        thread.join(10_000L);

        // Assert
        assertFalse(thread.isAlive(), "leak thread should finish on its own");
        assertEquals(3 * ONE_MB, thread.getPeakAllocatedBytes());
        assertEquals(0L, thread.getAllocatedBytes(), "all native memory must be released at the end");
    }

    @Test
    void unsafeMemoryAllocatesAndFrees() {
        final long address = UnsafeMemory.allocateAndTouch(ONE_MB);
        UnsafeMemory.free(address);
    }
}
