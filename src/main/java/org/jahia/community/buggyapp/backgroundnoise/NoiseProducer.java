package org.jahia.community.buggyapp.backgroundnoise;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.jahia.community.buggyapp.common.StoppableThread;

/**
 * Allocates a ~1 KB string every millisecond and stores it until the {@link NoiseConsumer} empties the store.
 */
public class NoiseProducer extends StoppableThread {

    private static final String PAYLOAD = "sample app creating large sample" + "e".repeat(1_000) + " object";
    private static final long PRODUCE_PAUSE_MS = 1L;
    private final Map<Long, String> store;
    private final AtomicLong produced;

    NoiseProducer(Map<Long, String> store, AtomicLong produced) {
        this.store = store;
        this.produced = produced;
    }

    @Override
    public void doStop() {
        super.doStop();
        interrupt();
    }

    @Override
    public void run() {
        while (keepRunning()) {
            final long id = produced.incrementAndGet();
            if (store.size() < BackgroundNoise.MAX_RETAINED_ENTRIES) {
                store.put(id, PAYLOAD + id);
            }
            try {
                Thread.sleep(PRODUCE_PAUSE_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
