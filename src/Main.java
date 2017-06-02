import controllers.MainController;
import handlers.JobHandler;
import handlers.PrefHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.LogUtils;

import java.util.logging.Logger;

/**
 * Основной класс приложения
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public class Main extends Application {

    private final static Logger logger = LogUtils.getLogger();
    private final static PrefHandler prefs = PrefHandler.getInstance();

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

        primaryStage.setWidth(prefs.getAppWidth());
        primaryStage.setHeight(prefs.getAppHeight());
        primaryStage.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> prefs.saveAppWidth(newSceneWidth));
        primaryStage.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> prefs.saveAppHeight(newSceneHeight));

        primaryStage.setOnCloseRequest(windowEvent -> {
            logger.info("Close application");
            prefs.saveAll();
            JobHandler.getInstance().stopScheduler();
        });

        primaryStage.getIcons().add(new Image("icon.png"));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
