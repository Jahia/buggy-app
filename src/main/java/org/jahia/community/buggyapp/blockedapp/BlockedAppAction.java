package org.jahia.community.buggyapp.blockedapp;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One thread takes a lock and keeps it; the other threads are BLOCKED waiting for it. Unlike {@code dead-lock},
 * the situation ends (and every thread is released) at the end of the duration or on Ctrl-C.
 */
@Command(scope = "buggy-app", name = "blocked-app", description = "Threads blocked on a lock held by another thread")
@Service
public class BlockedAppAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockedAppAction.class);
    private static final int MIN_THREADS = 2;

    @Option(name = "-d", aliases = "--duration", description = "Duration of the blocking in ms")
    long duration = 60_000L;

    @Option(name = "-n", aliases = "--number_of_threads", description = "Number of threads (one owner, the others blocked)")
    int numberOfThreads = 10;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        if (numberOfThreads < MIN_THREADS) {
            throw new IllegalArgumentException("number of threads must be >= " + MIN_THREADS + ", got " + numberOfThreads);
        }
        final AppObject appObject = new AppObject();
        final List<AppThread> threads = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new AppThread(appObject));
        }
        LOGGER.info("App started");
        runFor(duration, threads);
        LOGGER.info("Blocked app problem terminated!");
        return null;
    }
}
