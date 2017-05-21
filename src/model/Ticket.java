package model;

import handlers.DbHandler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Ticket {

    int id;
    int jobId;

    private transient final StringProperty lastName;
    private transient final StringProperty number;
    private transient final StringProperty flightNumber;
    private transient final ObjectProperty<LocalDate> date;


    // TODO для теста !! удалить после теста
    public Ticket() {
        this.lastName = new SimpleStringProperty("TEST");
        this.number = new SimpleStringProperty("123456");
        this.date = new SimpleObjectProperty<>(LocalDate.now().plusDays(1));
        this.flightNumber = new SimpleStringProperty("ZF9999");
    }

    public Ticket(int id, String lastName, String number, String date, String flightNumber, int jobId) {
        this(lastName, number, date, flightNumber, jobId);
        this.id = id;
    }

    public Ticket(String lastName, String number, String date, String flightNumber) {
        this.lastName = new SimpleStringProperty(lastName);
        this.number = new SimpleStringProperty(number);
        this.date = new SimpleObjectProperty<>(LocalDate.parse(date));
        this.flightNumber = new SimpleStringProperty(flightNumber);
    }

    public Ticket(String lastName, String number, String date, String flightNumber, int jobId) {
        this(lastName, number, date, flightNumber);
        this.jobId = jobId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getNumber() {
        return number.get();
    }

    public StringProperty numberProperty() {
        return number;
    }

    public void setNumber(String number) {
        this.number.set(number);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(LocalDate.parse(date));
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public String getFlightNumber() {
        return flightNumber.get();
    }

    public StringProperty flightNumberProperty() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber.set(flightNumber);
    }

    public void save() {
        DbHandler.getInstance().saveTicket(this);
    }

    public static Ticket get(int id) {
        return DbHandler.getInstance().getTicket(id);
    }
}
