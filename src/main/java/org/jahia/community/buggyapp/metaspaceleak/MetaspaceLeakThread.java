package org.jahia.community.buggyapp.metaspaceleak;

import javassist.CannotCompileException;
import javassist.ClassPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaspaceLeakThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakThread.class);
    private boolean doStop = false;

    public MetaspaceLeakThread() {
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return this.doStop == false;
    }

    @Override
    public void run() {
        try {
            int counter = 0;
            final ClassPool classPool = ClassPool.getDefault();
            while (keepRunning()) {
                classPool.makeClass("org.jahia.community.buggyapp.metaspaceleak.MetaspaceObject" + counter).toClass();
                counter++;
                if (counter % 50_000 == 0) {
                    LOGGER.info(counter + " new classes created");
                }
            }
        } catch (CannotCompileException ex) {
            LOGGER.error("Cannot compile", ex);
        }
    }
}
