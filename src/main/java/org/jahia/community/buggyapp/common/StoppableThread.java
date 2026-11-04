package org.jahia.community.buggyapp.common;

/**
 * Worker thread of a scenario that runs until {@link #doStop()} is called.
 *
 * <p>Worker threads deliberately keep the JVM default names ({@code Thread-N}): the training exercise asks trainees
 * to find the culprit from {@code top -H} / {@code iotop} and match the native thread id in a thread dump, which a
 * descriptive name would give away.</p>
 */
public abstract class StoppableThread extends Thread {

    private volatile boolean stopRequested;

    /** Asks the worker to stop; it exits at its next {@link #keepRunning()} check. */
    public void doStop() {
        stopRequested = true;
    }

    protected boolean keepRunning() {
        return !stopRequested;
    }
}
