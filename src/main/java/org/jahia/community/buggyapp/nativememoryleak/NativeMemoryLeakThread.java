package org.jahia.community.buggyapp.nativememoryleak;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import jdk.internal.ref.Cleaner;
import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

public class NativeMemoryLeakThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeMemoryLeakThread.class);
    private final long duration;
    private final long size;
    private final long totalSize;
    private boolean doStop = false;

    public NativeMemoryLeakThread(long duration, long size, long totalSize) {
        this.duration = duration;
        this.size = size;
        this.totalSize = totalSize;
    }

    @Override
    public void run() {
        LOGGER.info("Beginning of the leak");
        try {
            long currentSize = 0;
            final File file = createFile(FileUtil.TMP_DIR + File.separator + "native_memory_leak.bin", size);
            final Path path = Paths.get(file.getPath());
            final List<MappedByteBuffer> mappedByteBuffers = new ArrayList<>();
            while (currentSize < totalSize) {
                try ( FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ)) {
                    final MappedByteBuffer mappedByteBuffer = fileChannel.map(
                            FileChannel.MapMode.READ_ONLY,
                            0,
                            fileChannel.size());
                    mappedByteBuffers.add(mappedByteBuffer);
                    mappedByteBuffer.load();
                    currentSize += size;
                    Thread.sleep(duration / (totalSize / size));
                } catch (IOException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }

            Files.delete(path);
            for (MappedByteBuffer mappedByteBuffer : mappedByteBuffers) {
                invokeCleaner(mappedByteBuffer);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        LOGGER.info("End of the leak");
    }

    private void invokeCleaner(ByteBuffer directBuffer) {
        if (!directBuffer.isDirect()) {
            throw new IllegalArgumentException("buffer is non-direct");
        }

        final DirectBuffer db = (DirectBuffer) directBuffer;
        if (db.attachment() != null) {
            throw new IllegalArgumentException("duplicate or slice");
        }

        final Cleaner cleaner = db.cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    private File createFile(final String filename, final long sizeInBytes) throws IOException {
        final File file = new File(filename);
        file.createNewFile();
        try ( RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(sizeInBytes);
        }
        return file;
    }
}
