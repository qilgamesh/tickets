package model;

import java.time.LocalDateTime;

/**
 * Модель задания
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class Job {

    private int id;
    private String name;
    private JobState state;
    private LocalDateTime executeDate;
    private Airplane airplane;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = JobState.valueOf(state);
    }

    public LocalDateTime getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(LocalDateTime executeDate) {
        this.executeDate = executeDate;
    }

    public Airplane getAirplane() {
        return airplane;
    }

    public void setAirplane(Airplane airplane) {
        this.airplane = airplane;
    }
}
