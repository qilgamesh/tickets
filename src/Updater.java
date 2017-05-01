import controllers.UpdateController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Updater extends Thread {

    private final static String BASE_URL = "http://ekb.qilnet.ru:8080";
    private static String version;

    Updater(String version) {
        this.version = version;
    }

    public void start() {

        cleanup();

        try {
            if (checkUpdateNeed()) {
                update();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        System.out.println("Check update >> current version: " + version + ", last version: " + lastVersion);

        if (version.equals(lastVersion)) {
            return false;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/UpdateDialog.fxml"));
            GridPane page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Обновление");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UpdateController controller = loader.getController();
            controller.setVersion(lastVersion);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void update() {
        Thread worker = new Thread(() -> {
            try {
                downloadFile(BASE_URL + "/update.zip");
                unzip();
                launchUpdate();
            } catch (Exception ex) {
                System.out.println("error");
            }
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
            ex.printStackTrace();
        }

        System.exit(0);
    }

/*
    private String getDownloadLinkFromHost() throws IOException {

        InputStream html = new URL(BASE_URL + "/url").openConnection().getInputStream();

        int c = 0;
        StringBuilder buffer = new StringBuilder("");

        while (c != -1) {
            c = html.read();
            buffer.append((char) c);
        }

        return buffer.toString();
    }
*/

}
