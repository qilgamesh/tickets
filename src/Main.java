import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Main extends Application {

    private final static String VERSION = "0.0.1";
    private final static Logger logger = Log.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/Main.fxml"));
        primaryStage.setTitle("Tickets");
        primaryStage.setScene(new Scene(loader.load()));
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        controller.setVersion(VERSION);
        primaryStage.show();

        new Updater(primaryStage, VERSION).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
