package org.jahia.community.buggyapp.deadlock;

public class ThreadB extends Thread {

    public ThreadB() {
        super("buggy-app-deadlock-B");
    }

    @Override
    public void run() {
        HotObject.method2();
    }
}
