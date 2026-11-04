package org.jahia.community.buggyapp.backgroundnoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.jahia.community.buggyapp.common.Workers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Healthy but busy background activity, to run alongside another scenario so that the problem to find is not
 * alone on an idle JVM: several threads allocate continuously, the heap follows a sawtooth and the garbage
 * collector keeps working. Nothing leaks: everything is released at the end.
 */
public class BackgroundNoise extends StoppableThread {

    /** Upper bound of retained objects (~1 KB each), whatever the clear interval. */
    static final int MAX_RETAINED_ENTRIES = 50_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundNoise.class);
    private final long duration;
    private final int producers;
    private final long clearInterval;
    private final Map<Long, String> store = new ConcurrentHashMap<>();
    private final AtomicLong produced = new AtomicLong();
    private final AtomicLong clears = new AtomicLong();

    public BackgroundNoise(long duration, int producers, long clearInterval) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be > 0, got " + duration);
        }
        if (producers <= 0) {
            throw new IllegalArgumentException("number of producers must be > 0, got " + producers);
        }
        if (clearInterval <= 0) {
            throw new IllegalArgumentException("clear interval must be > 0, got " + clearInterval);
        }
        this.duration = duration;
        this.producers = producers;
        this.clearInterval = clearInterval;
    }

    @Override
    public void doStop() {
        super.doStop();
        // Wakes up Workers.runFor(), which then stops the producers and the consumer
        interrupt();
    }

    @Override
    public void run() {
        final List<StoppableThread> workers = new ArrayList<>(producers + 1);
        for (int i = 0; i < producers; i++) {
            workers.add(new NoiseProducer(store, produced));
        }
        workers.add(new NoiseConsumer(store, clearInterval, clears));
        workers.forEach(worker -> worker.setDaemon(true));
        LOGGER.info("Background noise started: {} producers, store emptied every {} ms", producers, clearInterval);
        try {
            Workers.runFor(duration, workers);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            store.clear();
            LOGGER.info("Background noise stopped after {} objects produced", produced.get());
        }
    }

    int getStoreSize() {
        return store.size();
    }

    long getProduced() {
        return produced.get();
    }

    long getClears() {
        return clears.get();
    }
}
