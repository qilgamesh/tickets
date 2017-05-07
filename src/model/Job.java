package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
    private transient final IntegerProperty tickets;

    public Job(int id, String name, String state, String executeDate) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleStringProperty(state);
        System.out.println(executeDate);
        this.departureDate = new SimpleStringProperty(executeDate);
        this.airplane = new SimpleStringProperty(null);
        this.tickets = new SimpleIntegerProperty(1);
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

    public StringProperty departureDateProperty() {
        return departureDate;
    }

    public void setExecuteDate(String executeDate) {
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

    public int getTickets() {
        return tickets.get();
    }

    public IntegerProperty ticketsProperty() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets.set(tickets);
    }
}
