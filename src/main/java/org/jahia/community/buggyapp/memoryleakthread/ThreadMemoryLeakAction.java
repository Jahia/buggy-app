package org.jahia.community.buggyapp.memoryleakthread;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "thread_memory-leak", description = "Memory Leak")
@Service
public class ThreadMemoryLeakAction implements Action {

    public static void start() {
        Object1 object1 = new Object1();
        object1.grow();
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
