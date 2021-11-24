package org.jahia.community.buggyapp.memoryleakwithoutcrash;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryLeakWithoutCrashThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakWithoutCrashThread.class);
    private final HashMap<Object, Object> myMap = new HashMap<>();
    private boolean doStop = false;
    private final long duration;

    public MemoryLeakWithoutCrashThread(long duration) {
        this.duration = duration;
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
            long counter = 0;
            while (keepRunning()) {
                float freeMemory = Runtime.getRuntime().freeMemory();
                float maxMemory = Runtime.getRuntime().maxMemory();
                if ((freeMemory / maxMemory) >= 0.01f) {
                    
                    myMap.put("key" + counter, "Large stringgggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg"
                            + counter);
                    ++counter;

                    if (counter % 1000 == 0) {
                        LOGGER.info("Inserted 1000 Records to map!");
                        Thread.sleep(duration / (Runtime.getRuntime().maxMemory() / (630000)));
                    }
                } else {
                    
                    Thread.sleep(1000L);
                    LOGGER.info("sleeping!");
                    LOGGER.info("max: " + Runtime.getRuntime().maxMemory() / (1024 * 1024)
                            + " (mb), total: "
                            + Runtime.getRuntime().totalMemory() / (1024 * 1024)
                            + " (mb), free: "
                            + Runtime.getRuntime().freeMemory() / (1024 * 1024));
                    
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Thread interrupted", ex);
        }
    }
}
