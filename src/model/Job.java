package model;

import handlers.DbHandler;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
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
    private transient final StringProperty departureDate;
    private transient final StringProperty airplane;
    private SimpleListProperty<Ticket> tickets = new SimpleListProperty<>(this, "tickets");

    public Job(String name, String state, String departureDate) {
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleStringProperty(state);

        this.departureDate = new SimpleStringProperty(departureDate);
        this.airplane = new SimpleStringProperty(null);
    }

    public Job(int id, String name, String state, String departureDate) {
        this(name, state, departureDate);
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

    public String getDepartureDate() {
        return departureDate.get();
    }

    public long getDepartureDateTimestamp() {
        return LocalDateTime.parse(getDepartureDate()).toInstant(ZoneOffset.ofHours(5)).getEpochSecond();
    }

    public StringProperty departureDateProperty() {
        return departureDate;
    }

    public void setDepartureDate(String executeDate) {
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
