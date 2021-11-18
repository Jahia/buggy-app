package org.jahia.community.buggyapp.oomcrash;

import java.util.HashMap;
import java.util.Map;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "cpu-spike", description = "Memory Leak")
@Service
public class OOMCrashAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(OOMCrashAction.class);

    public static void main(String args[]) {
        Map<String, String> map = new HashMap<>();

        long counter = 0;
        while (true) {
            counter++;
            map.put("key-" + counter, "value-" + counter);

            if (counter % 1000 == 0) {
                LOGGER.info("Added " + counter + " elements");
            }
        }
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
