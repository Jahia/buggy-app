package org.jahia.community.buggyapp.common;

/**
 * Worker thread of a scenario that runs until {@link #doStop()} is called.
 */
public abstract class StoppableThread extends Thread {

    private volatile boolean stopRequested;

    protected StoppableThread(String name) {
        super(name);
    }

    /** Asks the worker to stop; it exits at its next {@link #keepRunning()} check. */
    public void doStop() {
        stopRequested = true;
    }

    protected boolean keepRunning() {
        return !stopRequested;
    }
}
