package utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Логгер.
 * Подключает файл для логирования или создаёт новый при отсутствии
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class LogUtils {

    private static final Logger logger = Logger.getLogger(LogUtils.class.getName());
    private static boolean initialized = false;

    private static void init() throws IOException {

        File logDir = new File("logs");

        if (!logDir.exists()) {
            logDir.mkdir();
        }

        FileHandler logFile = new FileHandler("logs/tickets.log", true);
        logFile.setFormatter(new SimpleFormatter());

        logger.addHandler(logFile);
        initialized = true;
    }

    public static Logger getLogger() {

        if (!initialized) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
                return Logger.getLogger("");
            }
        }

        return logger;
    }
}
