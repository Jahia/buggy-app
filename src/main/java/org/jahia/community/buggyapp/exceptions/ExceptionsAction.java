package org.jahia.community.buggyapp.exceptions;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "exceptions", description = "Memory Leak")
@Service
public class ExceptionsAction implements Action{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsAction.class);
    public static long exceptionCounter = 0;

    public void start() {

        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            try {
                // Since dividing by zero it will result in Exception.
                int result = i / 0;
            } catch (Exception e) {
                ++exceptionCounter;
            }
        }

        LOGGER.info("Total Exceptions: " + exceptionCounter);
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
