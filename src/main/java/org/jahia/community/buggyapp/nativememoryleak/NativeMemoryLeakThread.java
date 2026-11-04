package org.jahia.community.buggyapp.nativememoryleak;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grows the process native memory (RSS) by memory-mapping and loading a {@code size}-byte file again and again,
 * until {@code totalSize} is mapped over {@code duration} ms, then unmaps everything and deletes the file.
 *
 * <p>The mapped buffers are what the training exercise looks for: {@code MappedByteBuffer} instances retained by
 * this thread in a heap dump, and the mapped {@code native_memory_leak*.bin} file in {@code pmap}.</p>
 */
public class NativeMemoryLeakThread extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeMemoryLeakThread.class);
    private static final long ONE_MB = 1024L * 1024L;
    private final long size;
    private final long totalSize;
    private final long pauseBetweenSteps;
    private volatile long allocatedBytes;
    private volatile long peakAllocatedBytes;
    private volatile Path file;

    public NativeMemoryLeakThread(long duration, long size, long totalSize) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0, got " + duration);
        }
        if (size <= 0 || size > Integer.MAX_VALUE) {
            // FileChannel.map() cannot map more than 2 GB in one buffer
            throw new IllegalArgumentException("size must be > 0 and <= " + Integer.MAX_VALUE + ", got " + size);
        }
        if (totalSize < size) {
            throw new IllegalArgumentException(String.format("total size (%s) must be >= size (%s)", totalSize, size));
        }
        this.size = size;
        this.totalSize = totalSize;
        this.pauseBetweenSteps = duration / (totalSize / size);
    }

    @Override
    public void doStop() {
        super.doStop();
        // Wake the thread up while it sleeps between two mappings
        interrupt();
    }

    @Override
    public void run() {
        LOGGER.info("Beginning of the leak");
        final List<MappedByteBuffer> mappedByteBuffers = new ArrayList<>();
        try {
            file = createFile(size);
            while (keepRunning() && allocatedBytes + size <= totalSize) {
                mappedByteBuffers.add(mapAndLoad(file));
                allocatedBytes += size;
                peakAllocatedBytes = allocatedBytes;
                LOGGER.info("{} MB of native memory mapped", allocatedBytes / ONE_MB);
                Thread.sleep(pauseBetweenSteps);
            }
        } catch (IOException ex) {
            // Stop on the first failure instead of retrying in a tight loop
            LOGGER.error("Native memory leak stopped after {} MB", allocatedBytes / ONE_MB, ex);
        } catch (InterruptedException ex) {
            LOGGER.info("Native memory leak stopped, releasing memory");
            Thread.currentThread().interrupt();
        } finally {
            releaseAll(mappedByteBuffers);
            FileUtil.deleteQuietly(file);
        }
        LOGGER.info("End of the leak");
    }

    private static Path createFile(long sizeInBytes) throws IOException {
        final Path path = Files.createTempFile("native_memory_leak", ".bin");
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.setLength(sizeInBytes);
        }
        return path;
    }

    private MappedByteBuffer mapAndLoad(Path path) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            // The mapping stays valid after the channel is closed
            final MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            mappedByteBuffer.load();
            return mappedByteBuffer;
        }
    }

    private void releaseAll(List<MappedByteBuffer> mappedByteBuffers) {
        for (MappedByteBuffer mappedByteBuffer : mappedByteBuffers) {
            try {
                DirectBufferCleaner.clean(mappedByteBuffer);
                allocatedBytes -= size;
            } catch (RuntimeException ex) {
                // Keep releasing the other buffers
                LOGGER.error("Failed to unmap a buffer", ex);
            }
        }
        mappedByteBuffers.clear();
    }

    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    public long getPeakAllocatedBytes() {
        return peakAllocatedBytes;
    }

    Path getFile() {
        return file;
    }
}
