package org.jahia.community.buggyapp.memoryleak;

import static org.jahia.community.buggyapp.TestWaits.letRun;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MemoryLeakActionTest {

    @Test
    void stopsWhenTheShellThreadIsInterrupted() throws InterruptedException {
        // Arrange: the scenario would otherwise run until OutOfMemoryError
        final Thread shell = new Thread(() -> {
            try {
                new MemoryLeakAction().execute();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });

        // Act: interrupt quickly, as Ctrl-C does, before the test heap fills up
        shell.start();
        letRun(20L);
        shell.interrupt();
        shell.join(5_000L);

        // Assert
        assertFalse(shell.isAlive(), "memory-leak must honour Ctrl-C");
    }
}
