package org.jahia.community.buggyapp.memoryleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "memory-leak", description = "Memory Leak")
@Service
public class MemoryLeakAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakAction.class);
    private static Object1 object1 = new Object1();

    public static void start() {
        try {
            object1.grow();
        } catch (Throwable e) {
            LOGGER.info(e.getMessage(), e);
        }

        LOGGER.info("Application is still running :-)");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error("Thread interrupted", ex);
        }

        LOGGER.info("Application terminated only now :-)");
    }

    public static void stop() {
        LOGGER.info("Out of memory error problem terminated!");
    }

    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
