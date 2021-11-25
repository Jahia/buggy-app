package org.jahia.community.buggyapp.threadleak;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "thread-leak", description = "Thread leak")
@Service
public class ThreadLeakAction implements Action {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLeakAction.class);
    private static final int NB_LOOPS = 50;
    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak")
    private long duration = 60000L;
    
    @Override
    public Object execute() throws Exception {
        LOGGER.info("Beginning of the leak");
        final List<ThreadLeakThread> threads = new ArrayList<>();
        for (int i = 0; i < NB_LOOPS; i++) {
            final ThreadLeakThread thread = new ThreadLeakThread();
            threads.add(thread);
            thread.start();
            Thread.sleep(duration / NB_LOOPS);
        }
        for (ThreadLeakThread thread : threads) {
            thread.closeUnderlyingConnections();
        }
        LOGGER.info("End of the leak");
        return null;
    }
}
