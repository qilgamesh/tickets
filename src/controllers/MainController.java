package controllers;

import handlers.JobHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Job;
import model.Ticket;

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

    public void handleAddJob(ActionEvent actionEvent) {

        Job job = new Job("job#" + (jobs.size() + 1), "ACTIVE", LocalDateTime.now().plusSeconds(3610));
        job.setTickets(FXCollections.observableArrayList(new Ticket()));
        jobs.add(job);

    }

    public void handleUpdateJob(ActionEvent actionEvent) {
        Job job = jobsTable.getSelectionModel().getSelectedItem();

        if (job != null) {
            job.setState("NEW");
            jobHandler.updateJob(job);
        }
    }
}
