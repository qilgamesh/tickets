package model;

import handlers.DbHandler;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
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

    private transient final ObjectProperty<JobState> state;
    private transient final ObjectProperty<LocalDateTime> departureDate;
    private transient final StringProperty flightNumber;
    private transient final IntegerProperty priorToReg;
    private ObservableList<Ticket> tickets = FXCollections.observableArrayList();

    public Job() {
        this.name = new SimpleStringProperty("Job#" + (DbHandler.getInstance().getJobsCount() + 1));
        this.state = new SimpleObjectProperty<>(JobState.NEW);
        this.departureDate = new SimpleObjectProperty<>(null);
        this.flightNumber = new SimpleStringProperty(null);
        this.priorToReg = new SimpleIntegerProperty(24);
    }

    public Job(String name, String state, LocalDateTime departureDate, String flightNumber, int priorToReg) {
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleObjectProperty<>(JobState.valueOf(state));
        this.departureDate = new SimpleObjectProperty<>(departureDate);
        this.flightNumber = new SimpleStringProperty(flightNumber);
        this.priorToReg = new SimpleIntegerProperty(priorToReg);
    }

    public Job(int id, String name, String state, String departureDateTimestamp, String flightNumber, int priorToReg) {
        this(name, state, LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(departureDateTimestamp)), ZoneId.of("GMT+5")), flightNumber, priorToReg);
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

    public JobState getState() {
        return state.get();
    }

    public ObjectProperty<JobState> stateProperty() {
        return state;
    }

    public void setState(String state) {
        this.state.set(JobState.valueOf(state));
    }

    public void setState(JobState state) {
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

    public String getFlightNumber() {
        return flightNumber.get();
    }

    public StringProperty flightNumberProperty() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber.set(flightNumber);
    }

    public int getPriorToReg() {
        return priorToReg.get();
    }

    public IntegerProperty priorToRegProperty() {
        return priorToReg;
    }

    public void setPriorToReg(int priorToReg) {
        this.priorToReg.set(priorToReg);
    }

    public ObservableList<Ticket> getTickets() {
        return tickets;
    }

    public StringProperty ticketsCount() {
        return new SimpleStringProperty(String.valueOf(tickets.size()));
    }

    public void setTickets(ObservableList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public synchronized void save() {
        DbHandler.getInstance().saveJob(this);
    }

    public static Job get(int id) {
        return DbHandler.getInstance().getJob(id);
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public boolean validate() {

        if (getTickets().size() == 0) {
            System.out.println("\n Job validate error: ticket size is 0");
            return false;
        }

        if (getPriorToReg() == 0) {
            System.out.println("\n Job validate error: priorToReg is 0");
            return false;
        }

        if (getDepartureDate().isBefore(LocalDateTime.now())) {
            System.out.println("\n Job validate error: departure date is before now");
            return false;
        }

        if (getFlightNumber() == null || getFlightNumber().equals("")) {
            System.out.println("\n Job validate error: flight number is blank");
            return false;
        }

        if (getName() == null || getName().equals("")) {
            System.out.println("\n Job validate error: name is blank");
            return false;
        }

        return true;
    }
}
