package org.jahia.community.buggyapp.nativememoryleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "native-memory-leak", description = "Native memory leak (runs in background)")
@Service
public class NativeMemoryLeakAction implements Action {

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    private long duration = 60_000L;
    @Option(name = "-s", aliases = "--size", description = "Size in bytes for each leak increase")
    private long size = 60L * 1024L * 1024L;
    @Option(name = "-t", aliases = "--total-size", description = "Size in bytes for the total increase")
    private long totalSize = 500L * 1024L * 1024L;

    @Override
    public Object execute() throws Exception {
        // Options are validated by the constructor, so bad input fails in the shell, not in the background
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(duration, size, totalSize);
        thread.start();
        return "Native memory leak started in background thread " + thread.getName();
    }
}
