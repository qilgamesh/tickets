package utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

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
        logFile.setFormatter(new Formatter() {
            public String format(LogRecord record) {
                return String.format("%s [%s] %s.%s  - %s\r\n",
                        SimpleDateFormat.getInstance().format(new Date()),
                        record.getLevel(),
                        record.getSourceClassName(),
                        record.getSourceMethodName(),
                        record.getMessage());
            }
        });

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
