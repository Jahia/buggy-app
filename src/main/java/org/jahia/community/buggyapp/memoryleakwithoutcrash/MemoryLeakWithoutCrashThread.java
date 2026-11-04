package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import java.util.HashMap;
import java.util.Map;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fills the heap up to about 99 % of {@code -Xmx}, holds it until stopped, then releases it.
 */
public class MemoryLeakWithoutCrashThread extends StoppableThread {

    /** Stop growing when less than this share of the maximum heap is left. */
    static final float MIN_HEADROOM_RATIO = 0.01f;
    /** Approximate heap retained by one batch of {@link #RECORDS_PER_BATCH} map entries. */
    static final long BYTES_PER_BATCH = 630_000L;
    private static final int RECORDS_PER_BATCH = 1000;
    private static final long FULL_HEAP_PAUSE_MS = 1000L;
    private static final long MB = 1024L * 1024L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakWithoutCrashThread.class);
    private static final String PAYLOAD = "Large string" + "g".repeat(611);

    private final Map<Object, Object> myMap = new HashMap<>();
    private final long pauseBetweenBatches;

    public MemoryLeakWithoutCrashThread(long duration) {
        this.pauseBetweenBatches = batchPause(duration, Runtime.getRuntime().maxMemory());
    }

    /**
     * Share of the maximum heap that is still available: {@code (max - used) / max}, where
     * {@code used = total - free}. {@code freeMemory()} alone only covers the currently committed heap, so with
     * {@code -Xms < -Xmx} the old {@code free / max} check stalled the leak far below the maximum.
     */
    static float headroomRatio(long maxMemory, long totalMemory, long freeMemory) {
        final long used = totalMemory - freeMemory;
        return (float) (maxMemory - used) / maxMemory;
    }

    /** Pause after each batch so that filling the whole heap takes about {@code duration} ms. */
    static long batchPause(long duration, long maxMemory) {
        final long batchesToFillHeap = Math.max(1L, maxMemory / BYTES_PER_BATCH);
        return duration / batchesToFillHeap;
    }

    @Override
    public void doStop() {
        super.doStop();
        // Wake the thread up if it is sleeping between batches or while the heap is full
        interrupt();
    }

    @Override
    public void run() {
        try {
            long counter = 0;
            while (keepRunning()) {
                if (currentHeadroomRatio() >= MIN_HEADROOM_RATIO) {
                    myMap.put("key" + counter, PAYLOAD + counter);
                    ++counter;
                    if (counter % RECORDS_PER_BATCH == 0) {
                        LOGGER.info("Inserted {} Records to map!", RECORDS_PER_BATCH);
                        Thread.sleep(pauseBetweenBatches);
                    }
                } else {
                    logHeapUsage();
                    Thread.sleep(FULL_HEAP_PAUSE_MS);
                }
            }
        } catch (InterruptedException ex) {
            // Interruption is how doStop() wakes us up: just stop
            Thread.currentThread().interrupt();
        } finally {
            myMap.clear();
            LOGGER.info("Leaked heap released");
        }
    }

    int getRetainedEntries() {
        return myMap.size();
    }

    private static float currentHeadroomRatio() {
        final Runtime runtime = Runtime.getRuntime();
        return headroomRatio(runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory());
    }

    private static void logHeapUsage() {
        final Runtime runtime = Runtime.getRuntime();
        LOGGER.info("Heap full, sleeping! max: {} (mb), total: {} (mb), free: {} (mb)",
                runtime.maxMemory() / MB, runtime.totalMemory() / MB, runtime.freeMemory() / MB);
    }
}
