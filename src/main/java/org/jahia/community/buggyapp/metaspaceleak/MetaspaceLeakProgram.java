package org.jahia.community.buggyapp.metaspaceleak;

import javassist.ClassPool;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "metaspace-leak", description = "Memory Leak")
@Service
public class MetaspaceLeakProgram {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakProgram.class);

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        ClassPool classPool = ClassPool.getDefault();
        for (int i = 0; i < 750_000; i++) {

            if (i % 50_000 == 0) {
                LOGGER.info(i + " new classes created");
            }

            // Keep creating classes dynamically!
            classPool.makeClass("org.jahia.community.buggyapp.metaspaceleak.MetaspaceObject" + i).toClass();
        }
        LOGGER.info("Program Exited: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }
}
