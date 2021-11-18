package org.jahia.community.buggyapp.inefficientlist;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "just-string", description = "Memory Leak")
@Service
public class JustAction implements Action {

    public static final String DEMO_STRING = "I am a demo string";
    private static final int MAX_ELEMENTS = 100000;
    private static String myString;

    public static void main(String args[]) {
        StringBuilder stringBuilder = new StringBuilder();

        int counter = 0;
        while (counter++ < MAX_ELEMENTS) {
            stringBuilder.append(DEMO_STRING);
        }

        myString = stringBuilder.toString();

        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (Exception e) {
        }
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
