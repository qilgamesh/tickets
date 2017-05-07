import handlers.DbHandler;
import javafx.collections.ObservableList;
import model.Job;
import model.JobState;
import utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
     * @param jobs
     */
    static void jobs(List<Job> jobs) {

        updateTime();

        for (Job job : jobs) {
            addJob(job);
        }
    }


    /**
     * Добавляет задание в планировщик
     *
     * @param job экземпляр задания
     */
    static void addJob(Job job) {

        if (job.getState().equals(JobState.COMPLETED.toString())) {
            return;
        }

        final Runnable jobRun = () -> {
            logger.info("Running job: " + job.getName());
            job.setState("COMPLETED");
        };

        long delay = ZonedDateTime.of(LocalDateTime.parse(job.getDepartureDate()), ZoneId.systemDefault()).toInstant().getEpochSecond() - zdt.toInstant().getEpochSecond() - 3600;
        logger.info("delay: " + delay + " сек");
        scheduler.schedule(jobRun, delay, TimeUnit.SECONDS);
    }

    /**
     * Берёт точное время из интернета, если не получается - системное
     */
    private static void updateTime() {

        URL url;

        try {
            url = new URL(timeUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            long unixTime = Long.valueOf(br.readLine());
            zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.of("GMT+5"));
            logger.info("Current date and time updated from web: " + zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to sync time from " + timeUrl, ex);
            logger.info("Get time from system");
            zdt = ZonedDateTime.now();
            logger.info("Current date and time: " + zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    /**
     * Завершает все задания планировщика
     */
    static void stop() {
        scheduler.shutdownNow();
    }
}
