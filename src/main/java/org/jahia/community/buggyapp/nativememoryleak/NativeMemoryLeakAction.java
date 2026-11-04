package org.jahia.community.buggyapp.nativememoryleak;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.jahia.community.buggyapp.common.BackgroundScenario;

/**
 * Starts the native memory leak in background. Only one leak runs at a time; {@code --stop} ends it early and
 * releases the memory.
 */
@Command(scope = "buggy-app", name = "native-memory-leak", description = "Native memory leak (runs in background)")
@Service
public class NativeMemoryLeakAction extends AbstractBuggyAction {

    private static final BackgroundScenario SCENARIO =
            new BackgroundScenario("native memory leak", "Native memory leak stopped, memory released");

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
        if (stop) {
            return SCENARIO.stop();
        }
        // Options are validated by the constructor, so bad input fails in the shell, not in the background
        return SCENARIO.start(new NativeMemoryLeakThread(duration, size, totalSize));
    }
}
