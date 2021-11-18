package org.jahia.community.buggyapp.cpuspike;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "cpu-spike", description = "CPU spike")
@Service
public class CPUSpikeAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPUSpikeAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the spike")
    private long duration = 60000L;

    @Option(name = "-n", aliases = "--number_of_threads", description = "Number of threads")
    private int numberOfThreads = 1;

    @Override
    public Object execute() throws Exception {
        final List<CPUSpikerThread> threads = new ArrayList<>();
        start(threads);
        LOGGER.info(String.format("Waiting %s ms before stopping", duration));
        Thread.sleep(duration);
        stop(threads);
        return null;
    }

    public void start(List<CPUSpikerThread> threads) {
        LOGGER.info("CPU spike beginning!");
        for (int i = 0; i < numberOfThreads; i++) {
            final CPUSpikerThread thread = new CPUSpikerThread();
            threads.add(thread);
            thread.start();
        }
        LOGGER.info(String.format("%s threads launched!", numberOfThreads));
    }

    public void stop(List<CPUSpikerThread> threads) {
        for (CPUSpikerThread thread : threads) {
            thread.doStop();
        }
        LOGGER.info("CPU spike terminated!");
    }
}
