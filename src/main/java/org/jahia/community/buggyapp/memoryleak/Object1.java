package org.jahia.community.buggyapp.memoryleak;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Object1 {

    private static final Logger LOGGER = LoggerFactory.getLogger(Object1.class);
    private final HashMap<Object, Object> myMap = new HashMap<>();

    public void grow() {
        long counter = 0;
        while (true) {
            if (counter % 1000 == 0) {
                LOGGER.info("Inserted 1000 Records to map!");
            }
            myMap.put("key" + counter, "Large stringgggggggggggggggggggggggggggg"
                    + "ggggggggggggggggggggggggggggggggggggggggggggggggggggg" + counter);
            ++counter;
        }
    }
}