package org.jahia.community.buggyapp.io;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "io", description = "Memory Leak")
@Service
public class IOAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(IOAction.class);

    public void start() {
        for (int counter = 1; counter <= 5; ++counter) {
            // Launch 5 threads.
            new IOThread("fileIO-" + counter + ".txt").start();
            LOGGER.info("Starting to write to fileIO-" + counter + ".txt");
        }
    }

    public static void stop() {
        LOGGER.info("Heavy IO activity terminated!");
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
