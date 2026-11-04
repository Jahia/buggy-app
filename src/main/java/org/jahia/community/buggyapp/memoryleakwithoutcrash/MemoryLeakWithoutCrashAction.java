package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "memory-leak-without-crash", description = "Memory leak without crash")
@Service
public class MemoryLeakWithoutCrashAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakWithoutCrashAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    long duration = 60_000L;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        LOGGER.info("Beginning of the leak");
        runFor(duration, List.of(new MemoryLeakWithoutCrashThread(duration)));
        LOGGER.info("End of the leak");
        return null;
    }
}
