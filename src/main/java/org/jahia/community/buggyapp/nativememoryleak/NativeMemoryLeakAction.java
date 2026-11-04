package org.jahia.community.buggyapp.nativememoryleak;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;

@Command(scope = "buggy-app", name = "native-memory-leak", description = "Native memory leak (runs in background)")
@Service
public class NativeMemoryLeakAction extends AbstractBuggyAction {

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    long duration = 60_000L;
    @Option(name = "-s", aliases = "--size", description = "Size in bytes for each leak increase")
    long size = 60L * 1024L * 1024L;
    @Option(name = "-t", aliases = "--total-size", description = "Size in bytes for the total increase")
    long totalSize = 500L * 1024L * 1024L;

    @Override
    protected Object doExecute() {
        // Options are validated by the constructor, so bad input fails in the shell, not in the background
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(duration, size, totalSize);
        thread.start();
        return "Native memory leak started in background thread " + thread.getName();
    }
}
