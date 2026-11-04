package org.jahia.community.buggyapp.nativememoryleak;

import java.util.ArrayList;
import java.util.List;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grows the process native (off-heap) memory by {@code size} bytes at regular intervals until {@code totalSize}
 * is reached over {@code duration} ms, then releases everything.
 */
public class NativeMemoryLeakThread extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeMemoryLeakThread.class);
    private static final long ONE_MB = 1024L * 1024L;
    private final long size;
    private final long totalSize;
    private final long pauseBetweenSteps;
    private volatile long allocatedBytes;
    private volatile long peakAllocatedBytes;

    public NativeMemoryLeakThread(long duration, long size, long totalSize) {
        super("buggy-app-native-memory-leak");
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0, got " + duration);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0, got " + size);
        }
        if (totalSize < size) {
            throw new IllegalArgumentException(String.format("total size (%s) must be >= size (%s)", totalSize, size));
        }
        this.size = size;
        this.totalSize = totalSize;
        this.pauseBetweenSteps = duration / (totalSize / size);
        // Runs in background: must never keep the JVM alive on shutdown
        setDaemon(true);
    }

    @Override
    public void doStop() {
        super.doStop();
        // Wake the thread up while it sleeps between two allocations
        interrupt();
    }

    @Override
    public void run() {
        LOGGER.info("Beginning of the leak");
        final List<Long> addresses = new ArrayList<>();
        try {
            while (keepRunning() && allocatedBytes + size <= totalSize) {
                addresses.add(UnsafeMemory.allocateAndTouch(size));
                allocatedBytes += size;
                peakAllocatedBytes = allocatedBytes;
                LOGGER.info("{} MB of native memory allocated", allocatedBytes / ONE_MB);
                Thread.sleep(pauseBetweenSteps);
            }
        } catch (InterruptedException ex) {
            LOGGER.info("Native memory leak stopped, releasing memory");
            Thread.currentThread().interrupt();
        } catch (OutOfMemoryError ex) {
            LOGGER.error("Native allocation failed after {} MB", allocatedBytes / ONE_MB, ex);
        } finally {
            releaseAll(addresses);
        }
        LOGGER.info("End of the leak");
    }

    private void releaseAll(List<Long> addresses) {
        for (long address : addresses) {
            try {
                UnsafeMemory.free(address);
                allocatedBytes -= size;
            } catch (RuntimeException ex) {
                // Keep releasing the other blocks
                LOGGER.error("Failed to free native block at {}", address, ex);
            }
        }
    }

    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    public long getPeakAllocatedBytes() {
        return peakAllocatedBytes;
    }
}
