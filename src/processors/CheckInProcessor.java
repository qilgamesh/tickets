package processors;

import handlers.PrefHandler;
import model.Job;
import model.JobState;
import utils.LogUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class CheckInProcessor implements Runnable {

    private final static Logger logger = LogUtils.getLogger();

    private Job job;

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

        try {
            String curTime = DateTimeProcessor.getCurrentDateTime().toLocalTime().toString();
            String message = URLEncoder.encode("<b>Старт:</b> " + job.getName() + ", <b>Время:</b> " + curTime, "UTF-8");
            HttpsURLConnection connection = (HttpsURLConnection) new URL(getTelegramUrl(message)).openConnection();
            connection.addRequestProperty("User-Agent", "Tickets app");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            logger.info("Job checkin result: " + sb.toString());

            job.setState(JobState.COMPLETED);
            job.save();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to checkin", ex);
            job.setState(JobState.ERROR);
            job.save();
        }
    }

    private String getTelegramUrl(String message) {

        String token = PrefHandler.getInstance().getTelegramToken();
        String chatId = PrefHandler.getInstance().getTelegramChatId();

        return "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&parse_mode=HTML&text=" + message;
    }
}
