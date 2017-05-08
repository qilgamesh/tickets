package controllers;

import handlers.JobHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Job;
import model.Ticket;

import java.time.LocalDateTime;


/**
 * Created by Andrey Semenyuk on 2017.
 */
public class MainController {

    private Stage primaryStage;
    private String version;

    @FXML
    private TableView<Job> jobsTable;
    @FXML
    private TableColumn<Job, String> nameCol;
    @FXML
    private TableColumn<Job, String> airplaneCol;
    @FXML
    private TableColumn<Job, String> ticketsCol;
    @FXML
    private TableColumn<Job, String> departureCol;
    @FXML
    private TableColumn<Job, String> stateCol;

    private ObservableList<Job> jobs;
    private JobHandler jobHandler;

    @FXML
    private void initialize() {

        jobHandler = JobHandler.getInstance();

        jobs = jobHandler.getJobs();
        jobsTable.setItems(this.jobs);

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        airplaneCol.setCellValueFactory(cellData -> cellData.getValue().airplaneProperty());
        ticketsCol.setCellValueFactory(cellData -> cellData.getValue().ticketsCount());
        departureCol.setCellValueFactory(cellData -> cellData.getValue().departureDateProperty());
        stateCol.setCellValueFactory(cellData -> cellData.getValue().stateProperty());
    }

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

    public void handleAddJob(ActionEvent actionEvent) {

        Job job = new Job("job#" + (jobs.size() + 1), "ACTIVE", LocalDateTime.now().plusSeconds(3610).toString());
        job.setTickets(FXCollections.observableArrayList(new Ticket()));
        jobs.add(job);

    }

    public void handleUpdateJob(ActionEvent actionEvent) {
        Job job = jobsTable.getSelectionModel().getSelectedItem();

        if (job != null) {
            jobHandler.updateJob(job);
        }
    }
}
