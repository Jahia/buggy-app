package org.jahia.community.buggyapp.heavyio;

import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeavyIOThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeavyIOThread.class);
    public static final String CONTENT
            = "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n"
            + "Hello World! We are building a simple chaos engineering product here. \n";
    private boolean doStop = false;
    public String fileName;

    public HeavyIOThread(String fileName) {
        this.fileName = fileName;
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return this.doStop == false;
    }

    @Override
    public void run() {
        int counter = 0;
        // Loop infinitely trying to read and close the file.
        while (keepRunning()) {
            // Write the contents to the file.
            FileUtil.write(fileName, CONTENT);

            // Read the contents from the file.
            FileUtil.read(fileName);

            if (++counter == 1000) {
                LOGGER.info("Read & write 1000 times to " + fileName);
                counter = 0;
            }
        }
    }
}
