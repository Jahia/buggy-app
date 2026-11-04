package org.jahia.community.buggyapp.nativememoryleak;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;

/**
 * Starts the native memory leak in background. Only one leak runs at a time; {@code --stop} ends it early and
 * releases the memory.
 */
@Command(scope = "buggy-app", name = "native-memory-leak", description = "Native memory leak (runs in background)")
@Service
public class NativeMemoryLeakAction extends AbstractBuggyAction {

    private static final AtomicReference<NativeMemoryLeakThread> RUNNING = new AtomicReference<>();
    private static final long STOP_TIMEOUT_MS = 10_000L;

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    long duration = 60_000L;
    @Option(name = "-s", aliases = "--size", description = "Size in bytes for each leak increase")
    long size = 60L * 1024L * 1024L;
    @Option(name = "-t", aliases = "--total-size", description = "Size in bytes for the total increase")
    long totalSize = 500L * 1024L * 1024L;
    @Option(name = "--stop", description = "Stop the running leak and release its memory")
    boolean stop = false;

    @Override
    protected Object doExecute() throws InterruptedException {
        return stop ? stopRunningLeak() : startLeak();
    }

    private String startLeak() {
        // Options are validated by the constructor, so bad input fails in the shell, not in the background
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(duration, size, totalSize);
        final NativeMemoryLeakThread current = RUNNING.get();
        if ((current != null && current.isAlive()) || !RUNNING.compareAndSet(current, thread)) {
            return "A native memory leak is already running; use --stop to end it";
        }
        thread.start();
        return "Native memory leak started in background thread " + thread.getName();
    }

    private static String stopRunningLeak() throws InterruptedException {
        final NativeMemoryLeakThread current = RUNNING.get();
        if (current == null || !current.isAlive()) {
            return "No native memory leak is running";
        }
        current.doStop();
        current.join(STOP_TIMEOUT_MS);
        return current.isAlive() ? "Stop requested, the leak is still releasing memory"
                : "Native memory leak stopped, memory released";
    }
}
