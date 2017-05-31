package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Job;
import model.JobState;
import model.Ticket;
import utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    @FXML
    private TableColumn<Ticket, Boolean> placeCol;
    @FXML
    private TableColumn<Ticket, Boolean> checkInCol;

    private Stage dialogStage;
    private Job job;
    private ObservableList<Ticket> tickets = FXCollections.observableArrayList();

    private static int MIN_PRIOR_TO_REG = 12;
    private static int MAX_PRIOR_TO_REG = 48;

    private final static Logger logger = LogUtils.getLogger();


    @FXML
    private void initialize() {

        ticketTable.setItems(tickets);

        MenuItem item1 = new MenuItem("Удалить");
        item1.setOnAction(event -> handleDeleteTicket());
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(item1);

        lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        lastNameCol.setStyle("-fx-alignment: CENTER;");

        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setStyle("-fx-alignment: CENTER;");

        checkInCol.setCellValueFactory(cellData -> cellData.getValue().checkInProperty());
        checkInCol.setStyle("-fx-alignment: CENTER;");
        checkInCol.setCellFactory(column -> new TableCell<Ticket, Boolean>() {
            final Button cellButton = new Button("Ручная");

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                cellButton.setOnAction(actionEvent -> handleCheckIn(job, (Ticket) getTableRow().getItem()));
                cellButton.setMinWidth(100);
                if (item != null && !empty && job != null && job.getState() == JobState.COMPLETED) {
                    setGraphic(cellButton);
                }
            }
        });

        descriptionField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || newValue.length() == 0) {
                descriptionField.setStyle("-fx-border-color: red");
            } else {
                descriptionField.setStyle("");
            }
        });

        flightNumberField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || newValue.length() == 0) {
                flightNumberField.setStyle("-fx-border-color: red");
            } else {
                flightNumberField.setStyle("");
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
/*

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
*/
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

        ticketTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ticketTable.setContextMenu(contextMenu);
        ticketTable.getContextMenu().setAutoHide(true);
    }

    private void handleDeleteTicket() {

        ObservableList<Ticket> selectedTickets = FXCollections.observableArrayList(ticketTable.getSelectionModel().getSelectedItems());

        for (Ticket ticket : selectedTickets) {
            ticket.delete();
        }

        tickets.removeAll(selectedTickets);
        ticketTable.refresh();
    }

    public void handleSave() {

        populateJobFields();

        boolean jobHasErrors = !job.validate();

        String errorMessage = null;

        if (jobHasErrors && tickets.size() == 0) {
            errorMessage = "Пожалуйста, заполните все поля и добавьте хотя бы один билет";
        } else {
            if (jobHasErrors) {
                errorMessage = "Пожалуйста, заполните все поля";
            }

            if (tickets.size() == 0) {
                errorMessage = "Пожалуйста, добавьте хотя бы один билет";
            }
        }

        if (errorMessage != null) {
            showErrorAlert(errorMessage);
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

        if (tickets.size() == 0) {
            addTicket();
        }

        job.setTickets(tickets);
        job.setPriorToReg(Integer.valueOf(priorToRegField.getText()));
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
        flightNumberField.setText(job.getFlightNumber());

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

        if (lastNameField.getText().length() > 0 && ticketNumberField.getText().length() > 0) {

            for (Ticket ticket : tickets) {
                if (ticket.getNumber().equals(ticketNumberField.getText())) {
                    ticketNumberField.setStyle("-fx-border-color: red");
                    return;
                }
            }

            ticketNumberField.setStyle("");
            lastNameField.setStyle("");

            Ticket ticket = new Ticket(lastNameField.getText(), ticketNumberField.getText());

            if (job.getId() > 0) {
                ticket.setJobId(job.getId());
            }

            tickets.add(ticket);
            lastNameField.clear();
            ticketNumberField.clear();
            ticketTable.refresh();
        } else {
            if (lastNameField.getText().length() == 0) {
                lastNameField.setStyle("-fx-border-color: red");
            }

            if (ticketNumberField.getText().length() == 0) {
                ticketNumberField.setStyle("-fx-border-color: red");
            }
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

    private String updateCookie(HttpURLConnection connection, String oldValue) {

        StringBuilder sb = new StringBuilder();
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");

        if (cookies == null) {
            return oldValue;
        }

        for (String cookie : cookies) {
            if (sb.length() > 0) {
                sb.append("; ");
            }

            String value = cookie.split(";")[0];
            sb.append(value);
        }

        return sb.toString();
    }

    private void handleCheckIn(Job job, Ticket ticket) {

        String baseUrl = "http://checkin.azurair.com/oxygen-check-in/json/";
        String params = "lastName=" + ticket.getLastName() + "&number=" + ticket.getNumber() + "&date=" +
                job.getDepartureDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "&flight=" + job.getFlightNumber();

        String cookieHeader = "JSESSIONID=541C3EAB31D86A528626A9874CB88584";

        try {
            URL url = new URL(baseUrl + "clear-session");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                if (!response.toString().contains("\"result\":\"ok\"")) {
                    logger.log(Level.SEVERE, "Failed to try clear session: " + response);
                    return;
                }
            }

            cookieHeader = updateCookie(connection, cookieHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(baseUrl + "add-to-order?" + params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (!response.toString().contains("\"result\":\"OK\"")) {
                    String errorMessage = "К сожалению, произошёл сбой обработки запроса. Попробуйте снова позже.";

                    if (response.toString().contains("orderNotFound")) {
                        errorMessage = "Билет не найден или регистрация ещё не началась.";
                    }

                    logger.log(Level.SEVERE,"Failed to checkin. Response: " + response);
                    showErrorAlert(errorMessage);
                    return;
                }
            }

            cookieHeader = updateCookie(connection, cookieHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(baseUrl + "order-info?" + params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", cookieHeader);
            cookieHeader = updateCookie(connection, cookieHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(baseUrl + "select-passengers?selected=%7B%22orderParts%22%3A%5B%7B%22passengers%22%3A%5Btrue%5D%7D%5D%7D");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", cookieHeader);
            cookieHeader = updateCookie(connection, cookieHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        String uri = "http://checkin.azurair.com/oxygen-check-in/#boardingPass";
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Set-Cookie", Arrays.asList(cookieHeader));

        try {
            CookieHandler.getDefault().put(URI.create("http://checkin.azurair.com/oxygen-check-in/"), headers);
            webEngine.load(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BorderPane page = new BorderPane(browser);
        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Регистрация на рейс");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setWidth(1200);
        dialogStage.showAndWait();
    }

    private void showErrorAlert(String errorMessage) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

}
