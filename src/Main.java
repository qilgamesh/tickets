import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private final static String VERSION = "0.0.2";

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

        new Updater(VERSION).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
