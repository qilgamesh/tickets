package handlers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.Job;
import model.JobState;
import utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик заданий
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public class JobHandler {

    // Адрес сервиса точного времени
    private final static String timeUrl = "https://time100.ru/api.php";

    private static JobHandler instance = null;
    // список заданий
    private static ObservableList<Job> jobs = FXCollections.observableArrayList();
    // Планировщик
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    // Список запланированных заданий
    private static Map<Job, ScheduledFuture<?>> runningJobs = new HashMap<>();

    private final static Logger logger = LogUtils.getLogger();

    /**
     * Получение экземляра обработчика
     *
     * @return JobHandler
     */
    public static JobHandler getInstance() {
        if (instance == null) {
            instance = new JobHandler();
        }
        return instance;
    }

    /**
     * Конструктор
     * Получает экземпляр обработчика БД, получает список активных заданий из БД,
     * добавляет из в планировщик на выполнение
     */
    private JobHandler() {

        // слушать на изменение списка заданий
        jobs.addListener((ListChangeListener<? super Job>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(job -> {
                        job.save();
                        scheduleJob(job);
                        logger.info("Added job name: " + job.getName());
                    });
                }
            }
        });

        jobs.addAll(DbHandler.getInstance().getActiveJobs());
    }

    /**
     * Добавить задание в планировщик
     *
     * @param job задание для добавления
     */
    public void scheduleJob(Job job) {

        if (job.getState().equals(JobState.COMPLETED.toString())) {
            return;
        }

        final Runnable jobRun = () -> {
            if (job.getState().equals("EDITABLE")) {
                return;
            }

            logger.info("Running job: " + job.getName());
            job.setState("RUNNING");

            try {
                Thread.sleep(job.getTickets().size() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            job.setState("COMPLETED");
            job.save();
        };

        long delay = job.getDepartureDateTimestamp() - getCurrentSecond() - 3600;
        logger.info("delay: " + delay + " сек");

        runningJobs.put(job, scheduler.schedule(jobRun, delay, TimeUnit.SECONDS));
        job.setState("ACTIVE");
    }

    /**
     * Удалить задание из запланированных
     *
     * @param job отменённое задание
     */
    public void removeJob(Job job) {

        ScheduledFuture<?> future = runningJobs.get(job);

        if (future != null) {
            future.cancel(true);
        }
    }


    /**
     * Обновить задание
     *
     * @param job изменённое задание
     */
    public void updateJob(Job job) {
        removeJob(job);
        job.save();
        job.setState("NEW");
        scheduleJob(job);
    }

    /**
     * Получает текущее время в секундах из сервиса точного времени, если не удаётся - берём системное
     *
     * @return long
     */
    private long getCurrentSecond() {
        URL url;

        try {
            url = new URL(timeUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return Long.valueOf(br.readLine());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to sync time from " + timeUrl, ex);
            logger.info("Get time from system");
            return System.currentTimeMillis() / 1000;
        }
    }

    /**
     * Получение текущих задач
     *
     * @return ObservableList<Job>
     */
    public ObservableList<Job> getJobs() {
        return jobs;
    }

    public void stopScheduler() {
        scheduler.shutdownNow();
    }
}
