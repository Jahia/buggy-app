package org.jahia.community.buggyapp.heavyio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "heavy-io", description = "Heavy IO")
@Service
public class HeavyIOAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeavyIOAction.class);
    @Option(name = "-d", aliases = "--duration", description = "Duration of the spike")
    private long duration = 60000L;

    @Option(name = "-n", aliases = "--number_of_threads", description = "Number of threads")
    private int numberOfThreads = 1;

    @Override
    public Object execute() throws Exception {
        final List<HeavyIOThread> threads = new ArrayList<>();
        start(threads);
        LOGGER.info(String.format("Waiting %s ms before stopping", duration));
        Thread.sleep(duration);
        stop(threads);
        return null;
    }

    public void start(List<HeavyIOThread> threads) {
        LOGGER.info("Heavy IO activity beginning!");
        for (int counter = 1; counter <= numberOfThreads; ++counter) {
            final String fileName = FileUtil.TMP_DIR + File.separator + "fileIO-" + counter + ".txt";
            final HeavyIOThread thread = new HeavyIOThread(fileName);
            threads.add(thread);
            thread.start();
            LOGGER.info(String.format("Starting to write to %s", fileName));
        }
        LOGGER.info(String.format("%s threads launched!", numberOfThreads));
    }

    public void stop(List<HeavyIOThread> threads) {
        for (HeavyIOThread thread : threads) {
            thread.doStop();
        }
        LOGGER.info("Heavy IO activity terminated!");
    }
}
