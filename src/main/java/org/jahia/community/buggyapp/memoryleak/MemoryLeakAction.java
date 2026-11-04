package org.jahia.community.buggyapp.memoryleak;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;

/**
 * Grows a map on the shell thread until the JVM throws {@link OutOfMemoryError}. Intentionally unbounded.
 */
@Command(scope = "buggy-app", name = "memory-leak", description = "Memory leak until OutOfMemoryError")
@Service
public class MemoryLeakAction extends AbstractBuggyAction {

    @Override
    protected Object doExecute() {
        new Object1().grow();
        return null;
    }
}
