package org.jahia.community.buggyapp.cpuspike;

import org.jahia.community.buggyapp.common.StoppableThread;

public class CPUSpikerThread extends StoppableThread {

    @Override
    public void run() {
        while (keepRunning()) {
            doSomething();
        }
    }

    public static void doSomething() {
        // Intentionally empty: the busy loop itself is the CPU spike
    }
}
