package org.jahia.community.buggyapp.backgroundnoise;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Empties the store at a fixed interval, turning the producers' allocations into garbage: the heap follows a
 * sawtooth and the garbage collector keeps working.
 */
public class NoiseConsumer extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoiseConsumer.class);
    private final Map<Long, String> store;
    private final long clearInterval;
    private final AtomicLong clears;

    NoiseConsumer(Map<Long, String> store, long clearInterval, AtomicLong clears) {
        this.store = store;
        this.clearInterval = clearInterval;
        this.clears = clears;
    }

    @Override
    public void doStop() {
        super.doStop();
        interrupt();
    }

    @Override
    public void run() {
        while (keepRunning()) {
            try {
                Thread.sleep(clearInterval);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
            final int size = store.size();
            store.clear();
            clears.incrementAndGet();
            LOGGER.debug("Consumer thread consumed {} objects", size);
        }
    }
}
