import controllers.MainController;
import handlers.JobHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.LogUtils;

import java.time.ZoneOffset;
import java.util.logging.Logger;

/**
 * Основной класс приложения
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public class Main extends Application {

    private final static Logger logger = LogUtils.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {

        Updater updater = Updater.getInstance();

        // проверяем обновления
        if (updater.checkUpdateNeed()) {
            updater.startUpdate();
        }

        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(getClass().getResource("view/Main.fxml"));
        BorderPane mainPane = mainLoader.load();
        MainController mainController = mainLoader.getController();
        mainController.setVersion(updater.getVersion());
        mainController.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.setTitle("Tickets");
        primaryStage.setOnCloseRequest(windowEvent -> {
            logger.info("Close application");
            JobHandler.getInstance().stopScheduler();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
