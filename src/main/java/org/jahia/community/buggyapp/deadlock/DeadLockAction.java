package org.jahia.community.buggyapp.deadlock;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts two threads that acquire the {@link CoolObject} and {@link HotObject} class monitors in opposite order.
 * The deadlock is permanent: the two threads stay blocked until the JVM is restarted.
 */
@Command(scope = "buggy-app", name = "dead-lock", description = "Dead lock (permanent until restart)")
@Service
public class DeadLockAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLockAction.class);

    @Override
    protected Object doExecute() {
        LOGGER.info("Starting deadlock threads");
        new ThreadA().start();
        new ThreadB().start();
        return null;
    }
}
