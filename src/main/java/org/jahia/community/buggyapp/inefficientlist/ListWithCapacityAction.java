package org.jahia.community.buggyapp.inefficientlist;

import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "list-with-capacity", description = "Memory Leak")
@Service
public class ListWithCapacityAction implements Action{

    public static final String DEMO_STRING = "I am a demo string";
    private static final int MAX_ELEMENTS = 100000;
    private static final List<List<String>> parentList = new ArrayList<>(MAX_ELEMENTS);

    public static void main(String args[]) {
        int counter = 0;
        while (counter++ < MAX_ELEMENTS) {
            List<String> childList = new ArrayList<>(1);
            childList.add(DEMO_STRING);
            parentList.add(childList);
        }
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
