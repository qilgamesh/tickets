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
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик заданий
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class JobHandler {

    // Адрес сервиса точного времени
    private final static String timeUrl = "https://time100.ru/api.php";

    private static JobHandler instance = null;
    private static DbHandler dbHandler;
    // список заданий
    private static ObservableList<Job> jobs;
    // Планировщик
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    // Список запланированных заданий
    private static Map<Job, ScheduledFuture<?>> runningJobs = new HashMap<>();

    private final static Logger logger = LogUtils.getLogger();


    /**
     * Конструктор
     * Получает экземпляр обработчика БД, получает список активных заданий из БД,
     * добавляет из в планировщик на выполнение
     */
    private JobHandler() {

        dbHandler = DbHandler.getInstance();
        logger.info("Loading active jobs from database");
        jobs = FXCollections.observableArrayList(dbHandler.getActiveJobs());
        jobs.forEach(this::scheduleJob);

        // слушать на изменение списка заданий
        jobs.addListener((ListChangeListener<? super Job>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Job job : change.getAddedSubList()) {
                        scheduleJob(job);
                        logger.info("Added job name: " + job.getName());
                    }
                }

                if (change.wasUpdated()) {
                    logger.info("jobs updated");
                }
            }
        });
    }

    /**
     * Добавить задание в планировщик
     *
     * @param job задание для добавления
     */
    private void scheduleJob(Job job) {

        if (job.getState().equals(JobState.COMPLETED.toString())) {
            return;
        }

        final Runnable jobRun = () -> {
            logger.info("Running job: " + job.getName());
            job.setState("RUNNING");

            try {
                Thread.sleep(job.getTickets().size()*1000);
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
    private void removeJob(Job job) {
        ScheduledFuture<?> future = runningJobs.get(job);
        future.cancel(true);
    }


    /**
     * Обновить задание
     *
     * @param job изменённое задание
     */
    public void updateJob(Job job) {
        removeJob(job);
        job.setDepartureDate(LocalDateTime.now().plusSeconds(3610).toString());
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
        logger.info("Get jobs called. Return " + jobs.size() + " elements");
        return jobs;
    }

    /**
     * Получение экземляра обработчика
     * @return JobHandler
     */
    public static JobHandler getInstance() {
        if (instance == null) {
            instance = new JobHandler();
        }
        return instance;
    }

    public void stopScheduler() {
        scheduler.shutdownNow();
    }
}
