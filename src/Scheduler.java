import Handlers.DbHandler;
import model.Job;
import utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Планировщик
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
class Scheduler {

    private final static String timeUrl = "https://time100.ru/api.php";
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final static Logger logger = LogUtils.getLogger();
    private static ZonedDateTime zdt;


    /**
     * Запускает все активные задания
     */
    static void jobs() {

        updateTime();

        DbHandler dbHandler = null;

        try {
            dbHandler = DbHandler.getInstance();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get instance of DbHandler: ", ex);
        }

        List<Job> jobs = dbHandler != null ? dbHandler.getActiveJobsTest() : null;

        for (Job job : jobs) {
            DbHandler finalDbHandler = dbHandler;
            final Runnable jobRun = () -> {
                logger.info("Running job: " + job.getName());
                job.setState("COMPLETED");
                finalDbHandler.updateJobState(job);
            };
            long delay = TimeUnit.MILLISECONDS.toSeconds(job.getExecuteDate().toInstant(ZoneOffset.ofHours(5)).toEpochMilli() - zdt.toInstant().toEpochMilli());
            logger.info("delay: " + delay + " сек");
            scheduler.schedule(jobRun, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * Берёт точное время из интернета, если не получается - берёт системное
     */
    private static void updateTime() {

        URL url;

        try {
            url = new URL(timeUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            long unixTime = Long.valueOf(br.readLine());
            zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.of("GMT+5"));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to sync time from " + timeUrl, ex);
            logger.info("Get time from system");
            zdt = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("GMT+5"));
        }

        logger.info("Current date: " + zdt);
    }

    /**
     * Завершает все задания планировщика
     */
    static void stop() {
        scheduler.shutdownNow();
    }
}
