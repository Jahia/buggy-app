package org.jahia.community.buggyapp.cpuspike;

public class CPUSpikerThread extends Thread {

    private boolean doStop = false;

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return this.doStop == false;
    }

    @Override
    public void run() {
        while (keepRunning()) {
            doSomething();
        }
    }

    public static void doSomething() {
    }
}
