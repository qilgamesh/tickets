import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Andrey Semenyuk on 2017.
 */
class Log {

    private static final Logger logger = Logger.getLogger(Log.class.getName());
    private static boolean initialized = false;

    private static void init() throws IOException {

        File logDir = new File("logs");

        if (!logDir.exists()) logDir.mkdir();

        FileHandler logFile = new FileHandler("logs/tickets.log", true);
        logFile.setFormatter(new SimpleFormatter());

        logger.addHandler(logFile);
        initialized = true;
    }

    static Logger getLogger() {

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
