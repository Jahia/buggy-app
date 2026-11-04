package org.jahia.community.buggyapp.threadleak;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "thread-leak", description = "Thread leak")
@Service
public class ThreadLeakAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLeakAction.class);
    private static final int NB_LOOPS = 50;

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    long duration = 60_000L;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        LOGGER.info("Beginning of the leak");
        final List<ThreadLeakThread> threads = new ArrayList<>(NB_LOOPS);
        try {
            for (int i = 0; i < NB_LOOPS; i++) {
                final ThreadLeakThread thread = new ThreadLeakThread();
                threads.add(thread);
                thread.start();
                Thread.sleep(duration / NB_LOOPS);
            }
        } finally {
            // Always release the leaked threads, including on Ctrl-C
            for (ThreadLeakThread thread : threads) {
                thread.close();
            }
        }
        LOGGER.info("End of the leak");
        return null;
    }
}
