package model;

import handlers.DbHandler;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;


/**
 * Модель задания
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public class Job {

    private int id;
    private transient final StringProperty name;

    private transient final StringProperty state;
    private transient final ObjectProperty<LocalDateTime> departureDate;
    private transient final StringProperty airplane;
    private SimpleListProperty<Ticket> tickets = new SimpleListProperty<>(this, "tickets");

    public Job() {
        this.name = new SimpleStringProperty("Job# " + (DbHandler.getInstance().getJobsCount() + 1));
        this.state = new SimpleStringProperty(null);
        this.departureDate = new SimpleObjectProperty<>(null);
        this.airplane = new SimpleStringProperty(null);
    }

    public Job(String name, String state, LocalDateTime departureDate, String airplane) {
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleStringProperty(state);
        this.departureDate = new SimpleObjectProperty<>(departureDate);
        this.airplane = new SimpleStringProperty(airplane);
    }

    public Job(int id, String name, String state, String departureDateTimestamp, String airplane) {
        this(name, state, LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(departureDateTimestamp)), ZoneId.of("GMT+5")), airplane);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getState() {
        return state.get();
    }

    public StringProperty stateProperty() {
        return state;
    }

    public void setState(String state) {
        this.state.set(state);
    }

    public LocalDateTime getDepartureDate() {
        return departureDate.get();
    }

    public long getDepartureDateTimestamp() {
        return getDepartureDate().toInstant(ZoneOffset.ofHours(5)).getEpochSecond();
    }

    public ObjectProperty<LocalDateTime> departureDateProperty() {
        return departureDate;
    }

    public void setDepartureDate(String executeDate) {
        this.departureDate.set(LocalDateTime.parse(executeDate));
    }

    public void setDepartureDate(LocalDateTime executeDate) {
        this.departureDate.set(executeDate);
    }

    public String getAirplane() {
        return airplane.get();
    }

    public StringProperty airplaneProperty() {
        return airplane;
    }

    public void setAirplane(String airplane) {
        this.airplane.set(airplane);
    }

    public ObservableList<Ticket> getTickets() {
        return tickets.get();
    }

    public SimpleListProperty<Ticket> ticketsProperty() {
        return tickets;
    }

    public StringProperty ticketsCount() {
        return new SimpleStringProperty(String.valueOf(tickets.size()));
    }

    public void setTickets(ObservableList<Ticket> tickets) {
        this.tickets.set(tickets);
    }

    public synchronized void save() {
        DbHandler.getInstance().saveJob(this);
    }

    public static Job get(int id) {
        return DbHandler.getInstance().getJob(id);
    }
}
