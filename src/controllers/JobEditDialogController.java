package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Job;
import model.JobState;
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
    private DatePicker departureDatePicker;
    @FXML
    private TextField departureTimeField;
    @FXML
    private TextField priorToRegField;
    @FXML
    private TextField flightNumberField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField ticketNumberField;
    @FXML
    private TableView<Ticket> ticketTable;
    @FXML
    private TableColumn<Ticket, String> lastNameCol;
    @FXML
    private TableColumn<Ticket, String> numberCol;

    private Stage dialogStage;
    private Job job;
    private ObservableList<Ticket> tickets = FXCollections.observableArrayList();

    private static int MIN_PRIOR_TO_REG = 12;
    private static int MAX_PRIOR_TO_REG = 48;

    @FXML
    private void initialize() {

        ticketTable.setItems(tickets);
        lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());

        descriptionField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || newValue.length() == 0) {
                descriptionField.setStyle("-fx-border-color: red");
            } else {
                descriptionField.setStyle("");
            }
        });

        UnaryOperator<TextFormatter.Change> filterTimeField = change -> {
            if (!change.isContentChange()) {
                return change;
            }

            switch (change.getControlText().length()) {
                case 0:
                    if (!change.getControlNewText().matches("[0-2]") && change.getControlNewText().length() != 4) {
                        return null;
                    }
                    break;
                case 1:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[0-9]|1[0-9]|2[0-3])")) {
                        return null;
                    }
                    break;
                case 2:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[0-9]|1[0-9]|2[0-3])(:|[0-5])")) {
                        return null;
                    }
                    break;
                case 3:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[0-9]|1[0-9]|2[0-3])(:[0-5]|[0-5][0-9])")) {
                        return null;
                    }
                    break;
                case 4:
                    if (!change.isDeleted() && !change.getControlNewText().matches("(0[0-9]|1[0-9]|2[0-3])(:[0-5][0-9])")) {
                        return null;
                    }
                    break;
            }

            return change;
        };

        StringConverter<String> converterTimeField = new StringConverter<String>() {
            @Override
            public String toString(String commitedText) {
                if (commitedText == null) {
                    return departureTimeField.getText();
                }

                if (commitedText.length() == 4 && commitedText.matches("(0[0-9]|1[0-9]|2[0-3])([0-5][0-9])")) {
                    return String.format("%s:%s", commitedText.substring(0, 2), commitedText.substring(2, 4));
                }
                return departureTimeField.getText();
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

        departureTimeField.setTextFormatter(new TextFormatter<>(converterTimeField, LocalTime.now().format(DateTimeFormatter.ofPattern("HHmm")), filterTimeField));

        departureDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
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

        departureDatePicker.setOnAction(actionEvent -> {
            if (departureDatePicker.getValue().isBefore(LocalDate.now())) {
                departureDatePicker.setValue(LocalDate.now());
            }
        });

        priorToRegField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                handleDecrement();
                keyEvent.consume();
            }

            if (keyEvent.getCode() == KeyCode.UP) {
                handleIncrement();
                keyEvent.consume();
            }
        });

        priorToRegField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.length() == 0 || !newValue.matches("\\d+")) {
                priorToRegField.setText("24");
                return;
            }

            if (oldValue.length() == 1 && Integer.valueOf(newValue) <= MIN_PRIOR_TO_REG) {
                priorToRegField.setText(String.valueOf(MIN_PRIOR_TO_REG));
                return;
            }

            if (Integer.valueOf(newValue) >= MAX_PRIOR_TO_REG) {
                priorToRegField.setText(String.valueOf(MAX_PRIOR_TO_REG));
            }
        });


        priorToRegField.focusedProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue) {
                if (Integer.valueOf(priorToRegField.getText()) <= MIN_PRIOR_TO_REG) {
                    priorToRegField.setText(String.valueOf(MIN_PRIOR_TO_REG));
                }
            }
        });
    }

    public void handleSave() {

        populateJobFields();

        if (!validateJob()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Пожалуйста, заполните все поля и добавьте хотя бы один билет");
            alert.showAndWait();
            return;
        }

        job.setState(JobState.NEW);
        dialogStage.close();
    }

    private void populateJobFields() {

        if (descriptionField.getText().length() > 0) {
            job.setName(descriptionField.getText());
        }

        if (departureDatePicker.getValue() != null && departureTimeField.getText().length() == 5) {
            job.setDepartureDate(LocalDateTime.of(departureDatePicker.getValue(), LocalTime.parse(departureTimeField.getText())));
        }

        if (flightNumberField.getText() != null) {
            job.setFlightNumber(flightNumberField.getText());
        }

        if (tickets.size() > 0) {
            for (Ticket ticket : tickets) {
                if (job.getId() > 0) {
                    ticket.setJobId(job.getId());
                }
            }

            job.setTickets(tickets);
        } else {
            if (lastNameField.getText().length() > 0 && ticketNumberField.getText().length() > 0) {
                Ticket ticket = new Ticket(lastNameField.getText(), ticketNumberField.getText());
                job.addTicket(ticket);
            }
        }

        job.setPriorToReg(Integer.valueOf(priorToRegField.getText()));
    }

    private boolean validateJob() {
        return job.validate() && validateTickets();
    }

    private boolean validateTickets() {

        for (Ticket ticket : job.getTickets()) {
            if (!ticket.validate()) {
                return false;
            }
        }

        return true;
    }

    public void handleCancel() {

        if (job.getId() > 0 && job.getState() != JobState.COMPLETED) {
            job.setState(JobState.NEW);
        }

        dialogStage.close();
    }

    void setJob(Job job) {

        if (job.getState() != JobState.COMPLETED) {
            job.setState(JobState.EDITABLE);
        }

        this.job = job;
        descriptionField.setText(job.getName());

        if (job.getDepartureDate() != null) {
            departureDatePicker.setValue(job.getDepartureDate().toLocalDate());
            departureTimeField.setText(job.getDepartureDate().toLocalTime().format(DateTimeFormatter.ofPattern("HHmm")));
        }

        if (job.getId() > 0) {
            tickets = job.getTickets();
            ticketTable.setItems(tickets);
        }

        if (job.getPriorToReg() != 24) {
            priorToRegField.setText(String.valueOf(job.getPriorToReg()));
        }
    }

    void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void addTicket() {

        if (lastNameField.getText() != null && ticketNumberField.getText() != null) {

            Ticket ticket = new Ticket(lastNameField.getText(), ticketNumberField.getText());

            if (job.getId() > 0) {
                ticket.setJobId(job.getId());
            }

            tickets.add(ticket);
        }
    }

    public void handleIncrement() {

        Integer value = Integer.valueOf(priorToRegField.getText());

        if (value >= MAX_PRIOR_TO_REG) {
            priorToRegField.setText(String.valueOf(MAX_PRIOR_TO_REG));
            return;
        }

        priorToRegField.setText((++value).toString());
    }

    public void handleDecrement() {

        Integer value = Integer.valueOf(priorToRegField.getText());

        if (value <= MIN_PRIOR_TO_REG) {
            priorToRegField.setText(String.valueOf(MIN_PRIOR_TO_REG));
            return;
        }

        priorToRegField.setText((--value).toString());
    }
}
