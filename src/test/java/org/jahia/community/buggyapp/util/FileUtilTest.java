package org.jahia.community.buggyapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void writesThenReadsBackTheContent() throws IOException {
        final Path file = tempDir.resolve("io.txt");

        FileUtil.write(file, "line1\nline2\n");

        assertEquals("line1\nline2\n", FileUtil.read(file));
    }

    @Test
    void readOfMissingFileFailsSoTheCallerCanStop() {
        assertThrows(IOException.class, () -> FileUtil.read(tempDir.resolve("missing.txt")));
    }

    @Test
    void writeToUnwritableLocationFailsSoTheCallerCanStop() {
        // tempDir is a directory: writing to it as a file fails
        assertThrows(IOException.class, () -> FileUtil.write(tempDir, "content"));
    }

    @Test
    void deleteQuietlyRemovesTheFileAndIgnoresMissingOnes() throws IOException {
        final Path file = tempDir.resolve("gone.txt");
        FileUtil.write(file, "x");

        FileUtil.deleteQuietly(file);
        FileUtil.deleteQuietly(file);
        FileUtil.deleteQuietly(null);

        assertFalse(file.toFile().exists());
    }
}
