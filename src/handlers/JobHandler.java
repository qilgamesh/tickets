package handlers;

import javafx.collections.FXCollections;
import model.Job;
import utils.LogUtils;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class JobHandler {

    private List<Job> jobs;
    private DbHandler dbHandler;
    private static JobHandler instance = null;

    private final static Logger logger = LogUtils.getLogger();

    private JobHandler() {
        dbHandler = DbHandler.getInstance();
        logger.info("Loading active jobs from database");
        jobs = FXCollections.observableArrayList(dbHandler.getActiveJobs());
    }

    public List<Job> getJobs() {
        logger.info("Get jobs called. Return " + jobs.size() + " elements");
        return jobs;
    }

    public static JobHandler getInstance() {
        if (instance == null) {
            instance = new JobHandler();
        }
        return instance;
    }
}
