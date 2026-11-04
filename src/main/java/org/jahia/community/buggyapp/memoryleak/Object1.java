package org.jahia.community.buggyapp.memoryleak;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Object1 {

    private static final Logger LOGGER = LoggerFactory.getLogger(Object1.class);
    private static final int RECORDS_PER_LOG = 1000;
    private static final String PAYLOAD = "Large string" + "g".repeat(81);
    private final Map<Object, Object> myMap = new HashMap<>();

    public void grow() {
        long counter = 0;
        while (true) {
            myMap.put("key" + counter, PAYLOAD + counter);
            ++counter;
            if (counter % RECORDS_PER_LOG == 0) {
                LOGGER.info("Inserted {} Records to map!", RECORDS_PER_LOG);
            }
        }
    }
}
