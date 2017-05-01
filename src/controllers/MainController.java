package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


/**
 * Created by Andrey Semenyuk on 2017.
 */
public class MainController {

    private Stage primaryStage;
    private String version;

    @FXML
    private void handleAbout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("О программе");
        alert.setHeaderText("Tickets");
        alert.setContentText("Версия: " + version);
        alert.showAndWait();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
