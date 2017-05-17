package processors;

import model.Job;
import model.JobState;
import utils.LogUtils;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class CheckInProcessor implements Runnable {

    private Job job;
    private final static Logger logger = LogUtils.getLogger();

    public CheckInProcessor(Job job) {
        this.job = job;
    }

    @Override
    public void run() {

        if (job.getState() == JobState.EDITABLE) {
            return;
        }

        logger.info("Running job: " + job.getName());
        job.setState(JobState.CHECKIN);
        Random random = new Random();

        try {
            Thread.sleep(job.getTickets().size() * 100 * random.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        job.setState(JobState.COMPLETED);
        job.save();
    }
}
