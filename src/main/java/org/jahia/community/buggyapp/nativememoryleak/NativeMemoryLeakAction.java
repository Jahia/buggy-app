package org.jahia.community.buggyapp.nativememoryleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "native-memory-leak", description = "Native memory leak")
@Service
public class NativeMemoryLeakAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeMemoryLeakAction.class);
    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak")
    private long duration = 60000L;
    @Option(name = "-s", aliases = "--size", description = "Size in bytes for each leak increase")
    private long size = 60 * 1024 * 1024;
    @Option(name = "-t", aliases = "--total-size", description = "Size in bytes for the total increase")
    private long totalSize = 500 * 1024 * 1024;

    @Override
    public Object execute() throws Exception {
        final NativeMemoryLeakThread thread = new NativeMemoryLeakThread(duration, size, totalSize);
        thread.start();
        return null;
    }

}
