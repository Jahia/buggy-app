package org.jahia.community.buggyapp.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    
    public static File createDirectory(String dir) {

        try {

            if (dir == null) {
                return null;
            }
            // creates the save directory if it does not exists
            File fileSaveDir = new File(dir);
            File fileSaveAbsDir = fileSaveDir.getAbsoluteFile();
            if (!fileSaveAbsDir.exists()) {
                fileSaveAbsDir.mkdirs();
            }

            return fileSaveAbsDir.getAbsoluteFile();
        } catch (Exception e) {

            LOGGER.info("Failed to create directory " + ExceptionUtil.getDetails(e));
        }

        return null;
    }

    public static void write(String filePath, String reportString) {

        try {

            File file = new File(filePath);
            createDirectory(file.getParent());

            Files.write(Paths.get(filePath), reportString.getBytes());
        } catch (Exception e) {

            LOGGER.error("Failed to write to filePath: " + filePath + ", " + ExceptionUtil.getDetails(e), e);
        }
    }

    public static String read(String filePath) {

        StringBuilder contentBuilder = new StringBuilder();

        try ( Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {

            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {

           LOGGER.error(e.getMessage(), e);
        }

        return contentBuilder.toString();
    }

}
