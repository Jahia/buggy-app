package org.jahia.community.buggyapp.heavyio;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "heavy-io", description = "Heavy IO")
@Service
public class HeavyIOAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeavyIOAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the I/O activity in ms")
    long duration = 60_000L;

    @Option(name = "-n", aliases = "--number_of_threads", description = "Number of threads")
    int numberOfThreads = 1;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        requirePositive(numberOfThreads, "number of threads");
        final List<HeavyIOThread> threads = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new HeavyIOThread());
        }
        LOGGER.info("Heavy IO activity beginning!");
        runFor(duration, threads);
        LOGGER.info("Heavy IO activity terminated!");
        return null;
    }
}
