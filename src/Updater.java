import Handlers.DbHandler;
import utils.LogUtils;
import controllers.UpdateController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Класс для проверки наличия обновлений приложения и его обновления
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class Updater {

    private final static Logger logger = LogUtils.getLogger();
    private final static String BASE_URL = "http://ekb.qilnet.ru:8080";
    private static String version;
    private static Updater instance;

    private Updater() {
        Updater.version = getVersion();
    }

    static Updater getInstance() {
        if (instance == null) {
            instance = new Updater();
        }
        return instance;
    }


    /**
     * Метод получения версии из манифеста приложения
     *
     * @return String текущая версия или null, если версию получить не удалось
     */
    String getVersion() {

        if (version != null) {
            return version;
        }

        try {
            Enumeration resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                InputStream stream = url.openStream();
                Manifest mf = new Manifest(stream);
                Attributes atts = mf.getMainAttributes();

                String title = atts.getValue(Attributes.Name.IMPLEMENTATION_TITLE);

                if (title != null && title.equals("Tickets")) {
                    return atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to get version", ex);
        }

        return null;
    }

    /**
     * Проверка необходимости обновления, отрисовка окна с оповещением о необходимости обновления с номером новой версии
     *
     * @return true - надо обновить, false - обновление не требуется или проверить не удалось
     */
    boolean checkUpdateNeed() {

        if (version == null) {
            return false;
        }

        String lastVersion;

        try {
            URL url = new URL(BASE_URL + "/version");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            lastVersion = br.readLine();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to check update", ex);
            return false;
        }

        logger.info("Check new version result: current version=" + version + ", lastVersion=" + lastVersion);

        if (version.equals(lastVersion)) {
            return false;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/UpdateDialog.fxml"));
            GridPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Обновление");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
//            dialogStage.initOwner(primaryStage);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(page));

            UpdateController controller = loader.getController();
            controller.setVersion(lastVersion);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to load update dialog view: ", ex);
        }

        return false;
    }

    /**
     * Главный метод обновления: удаляет старые обновления, если остались,
     * скачивает новый архив с обнолениями, извлекает во временную папку и запускает программу обновления
     */
    void start() {

        File f = new File("update.jar");

        if (f.exists()) {
            f.delete();
        }

        String updateFileLink = BASE_URL + "/update.zip";

        try {
            downloadFile(updateFileLink);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to download file " + updateFileLink + ": ", ex);
            return;
        }

        try {
            unzip();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to unpacking update.zip: ", ex);
            return;
        }

        logger.info("Launching update");

        String[] run = {"java", "-jar", "update.jar"};

        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to launch update", ex);
            return;
        }

        System.exit(0);
    }

    /**
     * Скачивание обновления
     *
     * @param link ссылка на архив с обновлениями
     * @throws IOException
     */
    private void downloadFile(String link) throws IOException {

        InputStream is = new URL(link).openConnection().getInputStream();
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File("update.zip")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            fOut.write(buffer, 0, bytesRead);
        }

        fOut.flush();
        fOut.close();
        is.close();
    }


    /**
     * Распаковка архива с обновлениями
     *
     * @throws IOException
     */
    private void unzip() throws IOException {

        int BUFFER = 2048;
        BufferedOutputStream dest;
        BufferedInputStream is;
        ZipEntry entry;
        ZipFile zipfile = new ZipFile("update.zip");
        Enumeration e = zipfile.entries();

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();

            if (entry.isDirectory()) {
                new File(entry.getName()).mkdir();
            } else {
                new File(entry.getName()).createNewFile();
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);

                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
                is.close();
            }
        }

        zipfile.close();
        new File("update.zip").delete();
    }
}
