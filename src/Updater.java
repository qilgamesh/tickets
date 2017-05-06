import controllers.UpdateController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Andrey Semenyuk on 2017.
 */
class Updater {

    private final static Logger logger = Log.getLogger();
    private final static String BASE_URL = "http://ekb.qilnet.ru:8080";
    private static String version;
    private static Stage primaryStage;

    Updater(Stage primaryStage, String version) {
        Updater.primaryStage = primaryStage;
        Updater.version = version;
    }

    void start() {

        cleanup();

        try {
            if (checkUpdateNeed()) {
                update();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void cleanup() {

        File f = new File("update.jar");

        if (f.exists()) {
            f.delete();
        }
    }

    private boolean checkUpdateNeed() throws Exception {

        URL url = new URL(BASE_URL + "/version");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String lastVersion = br.readLine();

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
            dialogStage.initOwner(primaryStage);
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

    private void update() {
        Thread worker = new Thread(() -> {
            String updateFileLink = BASE_URL + "/update.zip";

            try {
                downloadFile(updateFileLink);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to download file " + updateFileLink + ": ", ex);
            }

            try {
                unzip();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to unpacking update.zip: ", ex);
            }

            launchUpdate();
        });

        worker.start();
    }

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

    private void launchUpdate() {

        String[] run = {"java", "-jar", "update.jar"};

        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to launch update", ex);
        }

        System.exit(0);
    }
}
