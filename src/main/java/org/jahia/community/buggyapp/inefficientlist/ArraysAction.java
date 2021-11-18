package org.jahia.community.buggyapp.inefficientlist;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "arrays", description = "Memory Leak")
@Service
public class ArraysAction implements Action {

    public static final String DEMO_STRING = "I am a demo string";
    private static final int MAX_ELEMENTS = 100000;
    private static final String[][] parentArray = new String[MAX_ELEMENTS][1];

    public static void main(String args[]) {
        int counter = 0;
        while (counter < MAX_ELEMENTS) {
            String[] childArray = new String[1];
            childArray[0] = DEMO_STRING;
            parentArray[counter] = childArray;
        }
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
