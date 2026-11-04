package org.jahia.community.buggyapp.deadlock;

public class CoolObject {

    public static synchronized void method1() {
        LockHolder.holdLock();
        HotObject.method2();
    }
}
