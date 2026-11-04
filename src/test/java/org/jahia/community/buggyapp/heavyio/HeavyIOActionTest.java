package org.jahia.community.buggyapp.heavyio;

import static org.jahia.community.buggyapp.TestWaits.waitFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HeavyIOActionTest {

    private static final long TIMEOUT_MS = 5_000L;

    @Test
    void workerWritesToItsOwnFileAndDeletesItWhenStopped() throws InterruptedException {
        // Arrange
        final HeavyIOThread first = new HeavyIOThread();
        final HeavyIOThread second = new HeavyIOThread();

        // Act
        first.start();
        second.start();
        waitForFile(first);
        waitForFile(second);
        final Path firstFile = first.getFile();
        final Path secondFile = second.getFile();
        assertTrue(Files.exists(firstFile), "file should exist while the worker runs");
        first.doStop();
        second.doStop();
        first.join(TIMEOUT_MS);
        second.join(TIMEOUT_MS);

        // Assert
        assertNotEquals(firstFile, secondFile, "concurrent workers must not share a file");
        assertFalse(Files.exists(firstFile), "file must be deleted when the worker stops");
        assertFalse(Files.exists(secondFile), "file must be deleted when the worker stops");
    }

    @Test
    void actionRunsForTheDurationAndRejectsBadOptions() throws Exception {
        final HeavyIOAction action = new HeavyIOAction();
        action.duration = 50L;
        action.numberOfThreads = 1;
        assertEquals(null, action.execute());

        action.numberOfThreads = 0;
        assertThrows(IllegalArgumentException.class, action::execute);
    }


    @Test
    void workerStopsOnFirstIoFailureInsteadOfFloodingTheLog() throws Exception {
        final HeavyIOThread thread = new HeavyIOThread();
        thread.start();
        waitForFile(thread);

        // Replace the file with a directory so the next I/O fails. The worker may recreate the file between our
        // delete and createDirectory, or fail on its own as soon as the file is gone: retry until it stops.
        final Path file = thread.getFile();
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (thread.isAlive() && System.currentTimeMillis() < deadline) {
            try {
                Files.deleteIfExists(file);
                Files.createDirectory(file);
            } catch (java.nio.file.FileAlreadyExistsException | java.nio.file.NoSuchFileException retry) {
                // the worker raced us, try again
            }
            thread.join(10L);
        }

        assertFalse(thread.isAlive(), "the worker must stop on its own after an I/O failure");
        assertNotNull(thread.getFailure());
        Files.deleteIfExists(file);
    }

    private static void waitForFile(HeavyIOThread thread) {
        waitFor(() -> thread.getFile() != null);
        assertNotNull(thread.getFile());
    }
}
