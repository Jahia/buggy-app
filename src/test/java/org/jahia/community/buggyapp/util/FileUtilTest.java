package org.jahia.community.buggyapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void writesThenReadsBackTheContent() {
        final Path file = tempDir.resolve("io.txt");

        FileUtil.write(file, "line1\nline2\n");

        assertEquals("line1\nline2\n", FileUtil.read(file));
    }

    @Test
    void readOfMissingFileReturnsEmptyStringInsteadOfThrowing() {
        assertEquals("", FileUtil.read(tempDir.resolve("missing.txt")));
    }

    @Test
    void writeToUnwritableLocationIsLoggedNotThrown() {
        // tempDir is a directory: writing to it as a file fails
        FileUtil.write(tempDir, "content");
    }

    @Test
    void deleteQuietlyRemovesTheFileAndIgnoresMissingOnes() {
        final Path file = tempDir.resolve("gone.txt");
        FileUtil.write(file, "x");

        FileUtil.deleteQuietly(file);
        FileUtil.deleteQuietly(file);
        FileUtil.deleteQuietly(null);

        assertFalse(file.toFile().exists());
    }
}
