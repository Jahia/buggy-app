package org.jahia.community.buggyapp.nativememoryleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.file.Files;
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
    void mapsUpToTotalSizeThenUnmapsAndDeletesTheFile() throws InterruptedException {
        // Arrange
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(30L, ONE_MB, 3 * ONE_MB);

        // Act
        thread.start();
        thread.join(10_000L);

        // Assert
        assertFalse(thread.isAlive(), "leak thread should finish on its own");
        assertEquals(3 * ONE_MB, thread.getPeakAllocatedBytes());
        assertEquals(0L, thread.getAllocatedBytes(), "all mappings must be released at the end");
        assertNotNull(thread.getFile());
        assertFalse(Files.exists(thread.getFile()), "the mapped file must be deleted at the end");
    }

    @Test
    void cleanerRejectsHeapBuffers() {
        assertThrows(IllegalArgumentException.class, () -> DirectBufferCleaner.clean(ByteBuffer.allocate(16)));
    }

    @Test
    void cleanerFreesDirectBuffers() {
        DirectBufferCleaner.clean(ByteBuffer.allocateDirect(1024));
    }
}
