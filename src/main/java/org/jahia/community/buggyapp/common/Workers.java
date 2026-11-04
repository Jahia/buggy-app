package org.jahia.community.buggyapp.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a set of {@link StoppableThread} workers for a given duration.
 */
public final class Workers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Workers.class);
    private static final long JOIN_TIMEOUT_MS = 5_000L;

    private Workers() {
    }

    /**
     * Starts the workers, waits {@code durationMs}, then stops them. The workers are stopped in every case,
     * including when the calling thread is interrupted (Ctrl-C in the Karaf shell); the interruption is then
     * rethrown.
     */
    public static void runFor(long durationMs, List<? extends StoppableThread> workers) throws InterruptedException {
        try {
            for (StoppableThread worker : workers) {
                worker.start();
            }
            LOGGER.info("{} worker(s) started, waiting {} ms before stopping", workers.size(), durationMs);
            Thread.sleep(durationMs);
        } finally {
            stopAndJoin(workers);
        }
    }

    private static void stopAndJoin(List<? extends StoppableThread> workers) throws InterruptedException {
        for (StoppableThread worker : workers) {
            worker.doStop();
        }
        for (StoppableThread worker : workers) {
            worker.join(JOIN_TIMEOUT_MS);
            if (worker.isAlive()) {
                LOGGER.warn("Worker {} did not stop within {} ms", worker.getName(), JOIN_TIMEOUT_MS);
            }
        }
    }
}
