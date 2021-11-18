package org.jahia.community.buggyapp.threadleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "thread-leak", description = "Memory Leak")
@Service
public class ThreadLeakAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLeakAction.class);
    private static boolean flag = true;

    public static void setFlag(boolean newValue) {
        flag = newValue;
    }

    public static void start() {
        LOGGER.info("Thread App started");
        while (flag) {
            try {
                // Failed to put thread to sleep.
                Thread.sleep(100);
            } catch (Exception e) {
            }
            new ForeverThread().start();
        }
    }

    public static void stop() {
        LOGGER.info("Thread leak problem terminated!");
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
