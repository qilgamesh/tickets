package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Airplane;
import model.Job;
import model.Ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Andrey Semenyuk on 2017.
 */
public class JobEditDialogController {

    @FXML
    private TextField descriptionField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private ChoiceBox<Airplane> airplaneChoice;
    @FXML
    private TableView<Ticket> ticketTable;

    private Stage dialogStage;
    private Job job;
    private ObservableList<Ticket> tickets = FXCollections.observableArrayList();
    private ObservableList<Airplane> airplanes = FXCollections.observableArrayList(Airplane.list());
    private boolean hasErrors = false;

    @FXML
    private void initialize() {

        ticketTable.setItems(tickets);
        airplaneChoice.setItems(airplanes);

        descriptionField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || newValue.length() == 0) {
                descriptionField.setStyle("-fx-border-color: red");
            } else {
                descriptionField.setStyle("");
            }
        });

        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (!change.isContentChange()) {
                return change;
            }

            switch (change.getControlText().length()) {
                case 0:
                    if (!change.getControlNewText().matches("[0-2]")) {
                        return null;
                    }
                    break;
                case 1:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[1-9]|1[0-9]|2[0-3])")) {
                        return null;
                    }
                    break;
                case 2:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[1-9]|1[0-9]|2[0-3])(:|[0-5])")) {
                        return null;
                    }
                    break;
                case 3:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[1-9]|1[0-9]|2[0-3])(:[0-5]|[0-5][0-9])")) {
                        return null;
                    }
                    break;
                case 4:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[1-9]|1[0-9]|2[0-3])(:[0-5][0-9])")) {
                        return null;
                    }
                    break;
            }

            return change;
        };

        StringConverter<String> converter = new StringConverter<String>() {
            @Override
            public String toString(String commitedText) {
                if (commitedText == null) {
                    return timeField.getText();
                }

                if (commitedText.length() == 4 && commitedText.matches("(0[1-9]|1[0-9]|2[0-3])([0-5][0-9])")) {
                    return String.format("%s:%s", commitedText.substring(0, 2), commitedText.substring(2, 4));
                }
                return timeField.getText();
            }

            @Override
            public String fromString(String displayedText) {
                Pattern p = Pattern.compile("[\\p{Punct}\\p{Blank}]", Pattern.UNICODE_CHARACTER_CLASS);
                Matcher m = p.matcher(displayedText);
                displayedText = m.replaceAll("");

                if (displayedText.length() != 4) {
                    return null;
                }

                return displayedText;
            }
        };

        timeField.setTextFormatter(new TextFormatter<>(converter, LocalTime.now().format(DateTimeFormatter.ofPattern("HHmm")), filter));

        airplaneChoice.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> {
            job.setAirplane(airplanes.get((Integer) new_value).getName());
        });

        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: grey;");
                        } else {
                            datePicker.setStyle("");
                        }
                    }
                };
            }
        });

        datePicker.setOnAction(actionEvent -> {
            if (datePicker.getValue().isBefore(LocalDate.now())) {
                datePicker.setValue(LocalDate.now());
            }
        });
    }

    public void handleSave() {

        populateJobFields();

        if (validateJob()) {
            job.setState("NEW");
            dialogStage.close();
        }
    }

    private void populateJobFields() {

        if (descriptionField.getText().length() > 0) {
            job.setName(descriptionField.getText());
        }

        if (datePicker.getValue() != null && timeField.getText().length() == 5) {
            job.setDepartureDate(LocalDateTime.of(datePicker.getValue(), LocalTime.parse(timeField.getText())));
        }

        if (tickets.size() > 0) {
            job.setTickets(tickets);
        }
    }

    private boolean validateJob() {
        return job.getAirplane() != null && job.getName() != null && job.getDepartureDate() != null;
    }

    public void handleCancel() {
        dialogStage.close();
    }

    void setJob(Job job) {

        job.setState("EDITABLE");
        this.job = job;
        descriptionField.setText(job.getName());

        airplanes.forEach(airplane -> {
            if (airplane.getName().equals(job.getAirplane())) {
                airplaneChoice.setValue(airplane);
            }
        });

        if (job.getTickets() != null) {
            tickets = job.getTickets();
        }
    }

    void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void addTicket(ActionEvent actionEvent) {

        tickets.add(new Ticket("FAM", "1234567890", LocalDate.now().toString(), "SR301", 0));
    }

    Job getJob() {
        return job;
    }
}
