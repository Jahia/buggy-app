package org.jahia.community.buggyapp.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
    }

    public static void write(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static String read(Path file) throws IOException {
        final StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
            stream.forEach(line -> contentBuilder.append(line).append('\n'));
        }
        return contentBuilder.toString();
    }

    public static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            LOGGER.warn("Failed to delete {}", file, ex);
        }
    }
}
