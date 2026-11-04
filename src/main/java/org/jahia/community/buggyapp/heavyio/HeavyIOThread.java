package org.jahia.community.buggyapp.heavyio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private volatile Path file;
    private volatile IOException failure;

    @Override
    public void run() {
        try {
            file = Files.createTempFile("buggy-app-heavy-io-", ".txt");
            LOGGER.info("Starting to write to {}", file);
            int counter = 0;
            while (keepRunning()) {
                FileUtil.write(file, CONTENT);
                FileUtil.read(file);
                if (++counter == LOG_EVERY) {
                    LOGGER.info("Read & write {} times to {}", LOG_EVERY, file);
                    counter = 0;
                }
            }
        } catch (IOException ex) {
            // Stop at the first failure (disk full, file removed...) instead of flooding the log in a tight loop
            failure = ex;
            LOGGER.error("Heavy I/O worker {} stopped on I/O failure", getName(), ex);
        } finally {
            FileUtil.deleteQuietly(file);
        }
    }

    public Path getFile() {
        return file;
    }

    public IOException getFailure() {
        return failure;
    }
}
