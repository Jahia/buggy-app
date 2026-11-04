package org.jahia.community.buggyapp.blockedapp;

/**
 * Resource whose monitor is kept by the first thread that enters {@link #getSomething()} until {@link #release()}
 * is called: every other caller stays BLOCKED on it.
 *
 * <p>The owner sleeps while holding the lock ({@code Thread.sleep} keeps the monitor; {@code wait()} would release
 * it), checking the release flag at short intervals so the scenario can be ended promptly.</p>
 */
public class AppObject {

    private static final long HOLD_STEP_MS = 1_000L;
    private volatile boolean holding = true;

    public synchronized void getSomething() {
        while (holding) {
            try {
                // Deliberate: the scenario must keep the monitor while idle; wait() would release it
                Thread.sleep(HOLD_STEP_MS); // NOSONAR java:S2276 intentional fault
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** Lets the owner leave {@link #getSomething()}; the waiting threads then go through one by one. */
    public void release() {
        holding = false;
    }
}
