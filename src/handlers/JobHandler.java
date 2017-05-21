package handlers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import model.Job;
import model.JobState;
import processors.CheckInProcessor;
import processors.DateTimeProcessor;
import utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    private void scheduleJob(Job job) {

        Long delay = job.getDepartureDateTimestamp() - DateTimeProcessor.getCurrentSecond() - job.getPriorToReg() * 3600;

        if (job.getState() != JobState.NEW) {
            if (delay < 0) {
                job.setState(JobState.COMPLETED);
                job.save();
            }
            return;
        }

        Timeline countdown = new Timeline();
        countdown.setCycleCount(Timeline.INDEFINITE);
        job.setSecondsToReg(delay);
        countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            long newTime = job.getSecondsToReg() - 1;
            if (newTime <= 0 || job.getState() != JobState.ACTIVE) {
                job.setSecondsToReg(0);
                countdown.stop();
            }
            job.setSecondsToReg(newTime);
        }));

        countdown.playFromStart();
        runningJobs.put(job, scheduler.schedule(new CheckInProcessor(job), delay, TimeUnit.SECONDS));
        job.setState(JobState.ACTIVE);
    }

    /**
     * Удалить задание из запланированных
     *
     * @param job отменённое задание
     */
    private void removeJob(Job job) {

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
        job.setState(JobState.NEW);
        scheduleJob(job);
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
