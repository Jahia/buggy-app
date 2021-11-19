package org.jahia.community.buggyapp.memoryleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "buggy-app", name = "memory-leak", description = "Memory Leak")
@Service
public class MemoryLeakAction implements Action {

    @Override
    public Object execute() throws Exception {
        Object1 object1 = new Object1();
        object1.grow();
        return null;
    }
}
