package model;

import handlers.DbHandler;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Ticket {

    int id;
    int jobId;

    private transient final StringProperty lastName;
    private transient final StringProperty number;

    public Ticket(int id, String lastName, String number, int jobId) {
        this(lastName, number, jobId);
        this.id = id;
    }

    public Ticket(String lastName, String number) {
        this.lastName = new SimpleStringProperty(lastName);
        this.number = new SimpleStringProperty(number);
    }

    public Ticket(String lastName, String number, int jobId) {
        this(lastName, number);
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

    public void save() {
        DbHandler.getInstance().saveTicket(this);
    }

    public static Ticket get(int id) {
        return DbHandler.getInstance().getTicket(id);
    }

    public boolean validate() {

        if (getLastName() == null || getLastName().equals("")) {
            return false;
        }

        if (getNumber() == null || getNumber().equals("")) {
            return false;
        }

        return true;
    }

    public void delete() {
        DbHandler.getInstance().deleteTicket(id);
    }

    public ObservableValue<Boolean> checkInProperty() {
        return new SimpleBooleanProperty(true);
    }
}
