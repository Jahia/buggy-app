package org.jahia.community.buggyapp.memoryleakthread;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    HashMap<Object, Object> myMap = new HashMap<>();

    public void grow() {
        long counter = 0;
        while (true) {

            if (counter % 1000 == 0) {
                LOGGER.info("Inserted 1000 Records to map!");
            }

            try {
                //Thread.sleep(1);
            } catch (Exception e) {

            }
            myMap.put("key" + counter, "Large stringgggggggggggggggggggggggggggg"
                    + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg" + counter);
            ++counter;
        }
    }
}
