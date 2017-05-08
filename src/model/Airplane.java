package model;


/**
 * Модель самолёта
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class Airplane {

    private int id;

    private String name;
    private int rows;
    private int placesInRow;
    private int placesNearWindow;
    private Integer[] longLegsRows;

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

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getPlacesInRow() {
        return placesInRow;
    }

    public void setPlacesInRow(int placesInRow) {
        this.placesInRow = placesInRow;
    }

    public int getPlacesNearWindow() {
        return placesNearWindow;
    }

    public void setPlacesNearWindow(int placesNearWindow) {
        this.placesNearWindow = placesNearWindow;
    }

    public Integer[] getLongLegsRows() {
        return longLegsRows;
    }

    public void setLongLegsRows(Integer[] longLegsRows) {
        this.longLegsRows = longLegsRows;
    }

    public void save() {

    }
}
