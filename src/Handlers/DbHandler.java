package Handlers;

import model.Job;
import org.sqlite.JDBC;
import utils.LogUtils;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик для работы с базой данных
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public class DbHandler {
    // Константа, в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:db/tickets.sqlite";
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
                            "'execute_date' TIMESTAMP);"
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

    /**
     * Получение активных заданий
     *
     * @return List - список заданий или пустой список, если произошла ошибка
     */
    public List<Job> getActiveJobs() {

        try (Statement statement = this.connection.createStatement()) {
            List<Job> jobs = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery("SELECT id, name, execute_date, state FROM main.jobs WHERE state = 'active';");

            SimpleDateFormat DFTS = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            while (resultSet.next()) {
                jobs.add(new Job(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("state"),
                        resultSet.getTimestamp("execute_date").toLocalDateTime())
                );
            }

            return jobs;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get jobs: ", ex);
            return Collections.emptyList();
        }
    }

    /**
     * Получение активных заданий - тестовый
     *
     * @return List - список тестовых заданий
     */
    public List<Job> getActiveJobsTest() {

        List<Job> jobs = new ArrayList<>();

        jobs.add(new Job(1, "test1", "ACTIVE", LocalDateTime.now().plusMinutes(1)));
        jobs.add(new Job(2, "test2", "ACTIVE", LocalDateTime.now().plusMinutes(2)));
        jobs.add(new Job(3, "test3", "ACTIVE", LocalDateTime.now().plusMinutes(3)));
        jobs.add(new Job(4, "test4", "ACTIVE", LocalDateTime.now().plusMinutes(4)));

        return jobs;
    }

    public void updateJobState(Job job) {
        try (Statement statement = this.connection.createStatement()) {

//            statement.executeQuery("UPDATE jobs SET state id, name, execute_date, state FROM main.jobs WHERE state = 'active';");
            logger.info("Job update state");

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to update jobs ", ex);
        }
    }
}
