package org.jahia.community.buggyapp.deadlock;

public class HotObject {

    public static synchronized void method2() {
        LockHolder.holdLock();
        CoolObject.method1();
    }
}
