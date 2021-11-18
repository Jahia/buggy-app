package org.jahia.community.buggyapp.io;

import org.jahia.community.buggyapp.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOThread.class);
    private static boolean flag = true;
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
    public String fileName;

    public IOThread(String fileName) {
        this.fileName = fileName;
    }

    public static void setFlag(boolean newValue) {
        flag = newValue;
    }

    @Override
    public void run() {

        int counter = 0;
        // Loop infinitely trying to read and close the file.
        while (flag) {
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
