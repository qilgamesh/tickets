package controllers;

import handlers.JobHandler;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Job;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Created by Andrey Semenyuk on 2017.
 */
public class MainController {

    private Stage primaryStage;
    private String version;

    @FXML
    private Button updateButton;
    @FXML
    private TableView<Job> jobsTable;
    @FXML
    private TableColumn<Job, String> nameCol;
    @FXML
    private TableColumn<Job, String> airplaneCol;
    @FXML
    private TableColumn<Job, String> ticketsCol;
    @FXML
    private TableColumn<Job, LocalDateTime> departureCol;
    @FXML
    private TableColumn<Job, String> stateCol;

    private ObservableList<Job> jobs;
    private JobHandler jobHandler = JobHandler.getInstance();

    @FXML
    private void initialize() {

        jobs = jobHandler.getJobs();

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        airplaneCol.setCellValueFactory(cellData -> cellData.getValue().airplaneProperty());
        ticketsCol.setCellValueFactory(cellData -> cellData.getValue().ticketsCount());

        DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        departureCol.setCellFactory(column -> new TableCell<Job, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(myDateFormatter.format(item));
                }
            }
        });

        departureCol.setCellValueFactory(cellData -> cellData.getValue().departureDateProperty());
        stateCol.setCellValueFactory(cellData -> cellData.getValue().stateProperty());

        jobsTable.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> updateButton.setDisable(newValue == null));
        jobsTable.setItems(this.jobs);
    }

    @FXML
    private void handleAbout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("О программе");
        alert.setHeaderText(null);
        alert.setContentText("Tickets\nВерсия: " + version);
        alert.showAndWait();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private void showJobEditDialog(Job job) {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/JobEdit.fxml"));
            GridPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование транзакции");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            JobEditDialogController jobEditDialogController = loader.getController();
            jobEditDialogController.setDialogStage(dialogStage);
            jobEditDialogController.setJob(job);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAddJob(ActionEvent actionEvent) {

        Job newJob = new Job();
        showJobEditDialog(newJob);

        if (newJob.getState().equals("NEW")) {
            jobs.add(newJob);
        }
    }

    public void handleUpdateJob(ActionEvent actionEvent) {

        Job job = jobsTable.getSelectionModel().getSelectedItem();

        if (job != null) {
            showJobEditDialog(job);

            if (job.getState().equals("NEW")) {
                jobHandler.updateJob(job);
            }
        }
    }
}
