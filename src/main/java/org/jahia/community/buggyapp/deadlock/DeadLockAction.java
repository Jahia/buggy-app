package org.jahia.community.buggyapp.deadlock;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "memory-leak", description = "Memory Leak")
@Service
public class DeadLockAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLockAction.class);

    public static void start() {
        LOGGER.info("App started");
        new ThreadA().start();
        new ThreadB().start();
    }

    public static void stop() {
        LOGGER.info("Unsupported!");
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
