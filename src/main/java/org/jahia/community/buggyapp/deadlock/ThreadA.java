package org.jahia.community.buggyapp.deadlock;

public class ThreadA extends Thread {

    public ThreadA() {
        super("buggy-app-deadlock-A");
    }

    @Override
    public void run() {
        CoolObject.method1();
    }
}
