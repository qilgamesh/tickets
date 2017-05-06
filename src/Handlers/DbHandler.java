package Handlers;

import org.sqlite.JDBC;
import utils.LogUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик для работы с базой данных
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class DbHandler {
    // Константа, в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:db/tickets2.sqlite";
    // Объект, в котором будет храниться соединение с БД
    private Connection connection;
    // Используем singleton
    private static DbHandler instance = null;

    private final static Logger logger = LogUtils.getLogger();

    public static synchronized DbHandler getInstance() throws SQLException {
        if (instance == null) {
            instance = new DbHandler();
        }
        return instance;
    }

    /**
     * Конструктор обработчика
     * Загружает брайвер базы данных, создаёт соединение с БД или создаёт пустую при её отсутствии,
     * запускает инициализацию базы данных
     */
    private DbHandler() {

        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to register database driver: ", ex);
        }

        File dbDir = new File("db");

        if (!dbDir.exists()) {
            dbDir.mkdir();
        }

        try {
            this.connection = DriverManager.getConnection(CON_STR);
            initDatabase();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to connect to database");
        }
    }


    /**
     * Инициализация базы данных: создание таблиц, если их нет
     */
    private void initDatabase() {

        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'jobs' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'name' TEXT," +
                            "'state' TEXT," +
                            "'execute_date' DATETIME);"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'airplane' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'name' TEXT, 'rows' INTEGER," +
                            "'places_in_row' INTEGER," +
                            "'places_near_window' INTEGER," +
                            "'long_legs_rows' TEXT);"
            );
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create tables: ", ex);
        }
    }
}
