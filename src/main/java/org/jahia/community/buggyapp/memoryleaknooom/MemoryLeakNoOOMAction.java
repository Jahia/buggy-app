package org.jahia.community.buggyapp.memoryleaknooom;

import java.util.HashMap;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "memory-leak-no-oom", description = "Memory Leak")
@Service

public class MemoryLeakNoOOMAction implements Action{

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakNoOOMAction.class);
    private static final HashMap<Object, Object> myMap = new HashMap<>();
    private static boolean flag = true;

    public static void start() throws Exception {
        createObjects();
    }

    public static void stop() {
        LOGGER.info("Memory leak problem terminated!");
    }

    public static void setFlag(boolean newValue) {
        flag = newValue;
    }

    public static void createObjects() throws Exception {
        long counter = 0;

        while (flag) {
            // If free memory is less than 100 mb, then keep sleeping;
            //long freeMem = Runtime.getRuntime().maxMemory();
            /**
             * if (counter % 1000 == 0) { LOGGER.info("max: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "
             * (mb), total: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " (mb), free: " +
             * Runtime.getRuntime().freeMemory() / (1024 * 1024) ); }
             */
            if (((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) < 100 * 1024 * 1024)
                    && Runtime.getRuntime().freeMemory() < 50 * 1024 * 1024) {
                Thread.sleep(1000);
                LOGGER.info("sleeping!");
                LOGGER.info("max: " + Runtime.getRuntime().maxMemory() / (1024 * 1024)
                        + " (mb), total: "
                        + Runtime.getRuntime().totalMemory() / (1024 * 1024)
                        + " (mb), free: "
                        + Runtime.getRuntime().freeMemory() / (1024 * 1024));

                continue;
            }

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
            }
        }
    }

    @Override
    public Object execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
