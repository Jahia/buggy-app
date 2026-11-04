package org.jahia.community.buggyapp.deadlock;

/**
 * First lock of the deadlock: {@link #method1()} holds the {@code CoolObject} class monitor, then needs the
 * {@code HotObject} one.
 */
public final class CoolObject {

    private CoolObject() {
    }

    public static synchronized void method1() {
        LockHolder.holdLock();
        HotObject.touch();
    }

    /** Needs the {@code CoolObject} class monitor. */
    static synchronized void touch() {
        // Entering is enough: the monitor is what the other thread waits for
    }
}
