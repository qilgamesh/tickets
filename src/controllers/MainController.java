package controllers;

import handlers.JobHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Job;
import model.JobState;

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
    private TableColumn<Job, String> ticketsCol;
    @FXML
    private TableColumn<Job, LocalDateTime> departureCol;
    @FXML
    private TableColumn<Job, Number> timerToReg;
    @FXML
    private TableColumn<Job, JobState> stateCol;

    private ObservableList<Job> jobs;
    private JobHandler jobHandler = JobHandler.getInstance();

    @FXML
    private void initialize() {

        jobs = jobHandler.getJobs();

        MenuItem item1 = new MenuItem("Изменить");
        item1.setOnAction(event -> handleUpdateJob());
        MenuItem item2 = new MenuItem("Скрыть");
        item2.setOnAction(event -> handleArchiveJobs());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(item1, item2);

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        ticketsCol.setCellValueFactory(cellData -> cellData.getValue().ticketsCount());
        ticketsCol.setStyle("-fx-alignment: CENTER;");

        DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
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

        timerToReg.setCellValueFactory(cellData -> cellData.getValue().secondsToRegProperty());
        timerToReg.setCellFactory(tc -> new TableCell<Job, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);

                if (value == null || value.intValue() == 0 || empty) {
                    setText("");
                } else {
                    int hours = value.intValue() / 3600;
                    int min = (value.intValue() - hours * 3600) / 60;
                    int sec = value.intValue() - min * 60 - hours * 3600;
                    setText(String.format("%sч. %sм. %sс.", String.valueOf(hours), String.valueOf(min), String.valueOf(sec)));
                }
            }
        });

        stateCol.setCellValueFactory(cellData -> cellData.getValue().stateProperty());
        stateCol.setCellFactory(column -> new TableCell<Job, JobState>() {
            @Override
            protected void updateItem(JobState item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(item.getDescription());
                }
            }
        });
        stateCol.setStyle("-fx-alignment: CENTER;");

        jobsTable.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> updateButton.setDisable(newValue == null));
        jobsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        jobsTable.setContextMenu(contextMenu);
        jobsTable.getContextMenu().setAutoHide(true);

        jobsTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1) {
                handleUpdateJob();
            }
        });

        jobsTable.setItems(jobs);
    }

    private void handleArchiveJobs() {

        ObservableList<Job> selectedJobs = FXCollections.observableArrayList(jobsTable.getSelectionModel().getSelectedItems());

        for (Job job : selectedJobs) {
            if (job.getState() == JobState.COMPLETED) {
                job.setState(JobState.ARCHIVED);
                jobs.remove(job);
                job.save();
            }
        }

        jobsTable.refresh();
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
            dialogStage.setTitle("Редактирование задания");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);
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

    public void handleAddJob() {

        Job newJob = new Job();
        showJobEditDialog(newJob);

        if (newJob.getState() == JobState.NEW) {
            jobs.add(newJob);
        }
    }

    public void handleUpdateJob() {

        Job job = jobsTable.getSelectionModel().getSelectedItem();

        if (job != null) {
            showJobEditDialog(job);

            if (job.getState() == JobState.NEW) {
                jobHandler.updateJob(job);
                jobsTable.refresh();
            }
        }
    }
}
