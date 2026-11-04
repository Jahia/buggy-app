package org.jahia.community.buggyapp.blockedapp;

import org.jahia.community.buggyapp.common.StoppableThread;

public class AppThread extends StoppableThread {

    private final AppObject appObject;

    public AppThread(AppObject appObject) {
        this.appObject = appObject;
    }

    @Override
    public void run() {
        appObject.getSomething();
    }

    @Override
    public void doStop() {
        super.doStop();
        appObject.release();
        // Wakes the owner up if it is sleeping with the lock held
        interrupt();
    }
}
