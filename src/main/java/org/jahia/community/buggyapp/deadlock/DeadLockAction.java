package org.jahia.community.buggyapp.deadlock;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "deak-lock", description = "Dead lock")
@Service
public class DeadLockAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLockAction.class);

    @Override
    public Object execute() throws Exception {
        LOGGER.info("App started");
        new ThreadA().start();
        new ThreadB().start();
        return null;
    }
}
