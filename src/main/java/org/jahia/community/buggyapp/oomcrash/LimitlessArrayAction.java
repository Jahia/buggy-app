package org.jahia.community.buggyapp.oomcrash;

import java.util.UUID;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "cpu-spike", description = "Memory Leak")
@Service
public class LimitlessArrayAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitlessArrayAction.class);

    public static void main(String[] args) {
        LOGGER.info("The begining");
        final String[] bla = new String[800000000]; //crashes-right away
        //String[] bla = new String[400000000];

        for (int i = 0; i < 400000000; i++) {
            bla[i] = UUID.randomUUID().toString();
        }
        LOGGER.info("The end");
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
