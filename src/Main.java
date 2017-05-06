import Handlers.DbHandler;
import utils.LogUtils;
import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Основной класс приложения
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class Main extends Application {

    private final static Logger logger = LogUtils.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Updater updater = Updater.getInstance();

        // проверяем обновления
        if (updater.checkUpdateNeed()) {
            updater.start();
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/Main.fxml"));
        primaryStage.setTitle("Tickets");
        primaryStage.setScene(new Scene(loader.load()));
        MainController controller = loader.getController();
        controller.setVersion(updater.getVersion());
        controller.setPrimaryStage(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
