package model;

import handlers.DbHandler;

/**
 * Created by Andrey Semenyuk on 2017.
 */
public class Airline {

    int id;
    String name;
    String baseUrl;

    public Airline(int id, String name, String baseUrl) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
    }

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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static Airline get(int id) {
        return DbHandler.getInstance().getAirline(id);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
