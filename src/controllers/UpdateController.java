package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class UpdateController {

    @FXML
    private Button okButton;
    @FXML
    private Label detailsLabel;

    private Stage dialogStage;
    private boolean okClicked = false;

    public void setVersion(String ver) {
        this.detailsLabel.setText("Новая версия: " + ver);
    }

    @FXML
    private void handleOK() {
        okClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
