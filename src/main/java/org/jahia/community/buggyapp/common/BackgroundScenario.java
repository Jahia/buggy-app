package org.jahia.community.buggyapp.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A scenario that runs in background (the command returns immediately), one instance at a time, and that can be
 * ended early with {@code --stop}. Background threads are daemon threads: they never keep the JVM alive.
 */
public final class BackgroundScenario {

    private static final long STOP_TIMEOUT_MS = 10_000L;
    private final AtomicReference<StoppableThread> running = new AtomicReference<>();
    private final String label;
    private final String stoppedMessage;

    /**
     * @param label          lower-case name used in messages, e.g. "native memory leak"
     * @param stoppedMessage message returned once a running instance has been stopped
     */
    public BackgroundScenario(String label, String stoppedMessage) {
        this.label = label;
        this.stoppedMessage = stoppedMessage;
    }

    /** Starts {@code thread} unless an instance is already running. */
    public String start(StoppableThread thread) {
        final StoppableThread current = running.get();
        if ((current != null && current.isAlive()) || !running.compareAndSet(current, thread)) {
            return "A " + label + " is already running; use --stop to end it";
        }
        thread.setDaemon(true);
        thread.start();
        return Character.toUpperCase(label.charAt(0)) + label.substring(1) + " started in background thread "
                + thread.getName();
    }

    /** Stops the running instance, if any, and waits for it to release what it holds. */
    public String stop() throws InterruptedException {
        final StoppableThread current = running.get();
        if (current == null || !current.isAlive()) {
            return "No " + label + " is running";
        }
        current.doStop();
        current.join(STOP_TIMEOUT_MS);
        return current.isAlive() ? "Stop requested, the " + label + " is still stopping" : stoppedMessage;
    }
}
