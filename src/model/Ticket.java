package model;

import handlers.DbHandler;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Ticket {

    int id;
    String lastName;
    String number;
    String date;
    String flightNumber;
    int jobId;

    // TODO для теста !! удалить после теста
    public Ticket() {}

    public Ticket(int id, String lastName, String number, String date, String flightNumber, int jobId) {
        this(lastName, number, date, flightNumber, jobId);
        this.id = id;
    }

    public Ticket(String lastName, String number, String date, String flightNumber, int jobId) {
        this.lastName = lastName;
        this.number = number;
        this.date = date;
        this.flightNumber = flightNumber;
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
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void save() {
        DbHandler.getInstance().saveTicket(this);
    }

    public static Ticket get(int id) {
        return DbHandler.getInstance().getTicket(id);
    }
}
