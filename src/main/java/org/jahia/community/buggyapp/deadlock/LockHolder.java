package org.jahia.community.buggyapp.deadlock;

/**
 * Pause taken while holding the first monitor, so that the other thread grabs its own monitor first.
 */
final class LockHolder {

    private static final long HOLD_MS = 10_000L;

    private LockHolder() {
    }

    static void holdLock() {
        try {
            Thread.sleep(HOLD_MS);
        } catch (InterruptedException ex) {
            // Keep the interruption visible to the caller instead of swallowing it
            Thread.currentThread().interrupt();
        }
    }
}
