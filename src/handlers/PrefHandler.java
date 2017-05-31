package handlers;

import java.io.*;
import java.util.Properties;


/**
 * Обработчик настроек приложения
 * Created by Andrey Semenyuk on 2017.
 */
public class PrefHandler {

    private static PrefHandler instance = null;
    private static Properties appProperties = new Properties();

    /**
     * Получение экземляра обработчика
     *
     * @return JobHandler
     */
    public static PrefHandler getInstance() {
        if (instance == null) {
            instance = new PrefHandler();
        }
        return instance;
    }

    private PrefHandler() {

        File f = new File("config.properties");

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ignored) {}
        }

        try {
            appProperties.load(new FileInputStream(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTelegramToken() {
        return appProperties.getProperty("telegram.token", "312066192:AAHEbbCcYqkj463wVDIm0C8ou_sRMEPvbIc");
    }

    public String getTelegramChatId() {
        return appProperties.getProperty("telegram.chat", "-228942046");
    }

    public void saveAppWidth(Number newSceneWidth) {
        appProperties.setProperty("app.width", newSceneWidth.toString());
    }

    public void saveAppHeight(Number newSceneHeight) {
        appProperties.setProperty("app.height", newSceneHeight.toString());
    }

    public void saveAll() {
        try {
            appProperties.store(new FileOutputStream(new File("config.properties")), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAppWidth() {
        return Double.valueOf(appProperties.getProperty("app.width", "800"));
    }

    public double getAppHeight() {
        return Double.valueOf(appProperties.getProperty("app.height", "600"));
    }
}
