package org.jahia.community.buggyapp.heavyio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes then reads back a small file in a tight loop until stopped. Each worker uses its own temporary file,
 * deleted when the worker stops.
 */
public class HeavyIOThread extends StoppableThread {

    public static final String CONTENT = "Hello World! We are building a simple chaos engineering product here. \n"
            .repeat(17);
    private static final Logger LOGGER = LoggerFactory.getLogger(HeavyIOThread.class);
    private static final int LOG_EVERY = 1000;
    private final AtomicReference<Path> file = new AtomicReference<>();
    private final AtomicReference<IOException> failure = new AtomicReference<>();

    @Override
    public void run() {
        try {
            final Path ioFile = Files.createTempFile("buggy-app-heavy-io-", ".txt");
            file.set(ioFile);
            LOGGER.info("Starting to write to {}", ioFile);
            int counter = 0;
            while (keepRunning()) {
                FileUtil.write(ioFile, CONTENT);
                FileUtil.read(ioFile);
                if (++counter == LOG_EVERY) {
                    LOGGER.info("Read & write {} times to {}", LOG_EVERY, ioFile);
                    counter = 0;
                }
            }
        } catch (IOException ex) {
            // Stop at the first failure (disk full, file removed...) instead of flooding the log in a tight loop
            failure.set(ex);
            LOGGER.error("Heavy I/O worker {} stopped on I/O failure", getName(), ex);
        } finally {
            FileUtil.deleteQuietly(file.get());
        }
    }

    public Path getFile() {
        return file.get();
    }

    public IOException getFailure() {
        return failure.get();
    }
}
