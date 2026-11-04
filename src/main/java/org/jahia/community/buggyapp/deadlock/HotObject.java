package org.jahia.community.buggyapp.deadlock;

/**
 * Second lock of the deadlock: {@link #method2()} holds the {@code HotObject} class monitor, then needs the
 * {@code CoolObject} one. Taken in the opposite order to {@link CoolObject#method1()}: classic lock-ordering
 * deadlock, without recursion.
 */
public final class HotObject {

    private HotObject() {
    }

    public static synchronized void method2() {
        LockHolder.holdLock();
        CoolObject.touch();
    }

    /** Needs the {@code HotObject} class monitor. */
    static synchronized void touch() {
        // Entering is enough: the monitor is what the other thread waits for
    }
}
