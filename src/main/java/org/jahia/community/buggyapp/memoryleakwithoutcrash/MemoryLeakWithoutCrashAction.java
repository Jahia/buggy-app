package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "memory-leak-without-crash", description = "Memory leak without crash")
@Service

public class MemoryLeakWithoutCrashAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakWithoutCrashAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak")
    private long duration = 60000L;

    @Override
    public Object execute() throws Exception {
        LOGGER.info("Beginning of the leak");
        final MemoryLeakWithoutCrashThread thread = new MemoryLeakWithoutCrashThread(duration);
        thread.start();
        Thread.sleep(duration);
        thread.doStop();
        LOGGER.info("End of the leak");
        return null;
    }
}
