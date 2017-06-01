package handlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Airplane;
import model.Job;
import model.JobState;
import model.Ticket;
import org.sqlite.JDBC;
import utils.LogUtils;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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

    public static synchronized DbHandler getInstance() {
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
                    "CREATE TABLE IF NOT EXISTS 'job' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'name' TEXT," +
                            "'state' TEXT," +
                            "'departure_date' TIMESTAMP," +
                            "'prior_to_reg' INTEGER DEFAULT 24," +
                            "'flight_number' TEXT);"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'airplane' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'name' TEXT, 'rows' INTEGER," +
                            "'places_in_row' INTEGER," +
                            "'places_near_window' INTEGER," +
                            "'long_legs_rows' TEXT);"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'ticket' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'last_name' TEXT," +
                            "'number' TEXT," +
                            "'date' TEXT," +
                            "'job_id' INTEGER REFERENCES job (id));"
            );
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create tables: ", ex);
        }
    }

    /**
     * Запуск миграции базы данных
     *
     * @param in входящий поток из файла со скриптом
     */
    public void executeMigrate(InputStream in) {

        logger.info("Start migrate");

        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("((\r)?\n)");

        try {
            Statement statement = this.connection.createStatement();

            while (scanner.hasNext()) {
                String line = scanner.next();

                if (line.trim().length() > 0) {
                    logger.info("Migrate: execute line: " + line);
                    statement.execute(line);
                }
            }

            logger.info("End migrate");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to execute migrate tables: ", ex);
        }
    }

    /**
     * Получение активных заданий
     *
     * @return List - список заданий
     */
    public List<Job> getActiveJobs() {

        List<Job> jobs = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM main.job WHERE NOT state = 'ARCHIVED';");

            while (rs.next()) {
                Job job = new Job(rs.getInt("id"), rs.getString("name"), rs.getString("state"), rs.getString("departure_date"), rs.getString("flight_number"), rs.getInt("prior_to_reg"));
                job.setTickets(getTicketByJobId(job.getId()));
                job.setName("Job#" + job.getId());
                jobs.add(job);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get jobs: ", ex);
        }

        return jobs;
    }

    public int getJobsCount() {

        int count = 0;

        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM job;");

            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get jobs: ", ex);
        }

        return count;
    }

    /**
     * Получение билетов по id заданию
     *
     * @param id задания
     * @return Ticket
     */
    private ObservableList<Ticket> getTicketByJobId(int id) {

        ObservableList<Ticket> tickets = FXCollections.observableArrayList();

        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM ticket WHERE job_id = " + id);

            while (rs.next()) {
                tickets.add(new Ticket(rs.getInt("id"), rs.getString("last_name"), rs.getString("number"), id));
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get tickets for job id= " + id, ex);
        }

        return tickets;
    }

    public Job getJob(int id) {

        if (id == 0) {
            return null;
        }

        try {
            Statement statement = this.connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM job WHERE id = " + id);
            rs.next();

            return new Job(id, rs.getString("name"), rs.getString("state"), rs.getString("departure_date"), rs.getString("flight_number"), rs.getInt("prior_to_reg"));

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create statement", ex);
        }

        return null;
    }

    /**
     * Сохранение или добавление нового задания в БД.
     *
     * @param job новое или изменённое задание
     */
    public synchronized void saveJob(Job job) {

        Job savedJob = getJob(job.getId());
        String smnt = "UPDATE job SET (name, state, departure_date, flight_number, prior_to_reg) = (?, ?, ?, ?, ?) WHERE id = " + job.getId() + ";";

        if (savedJob == null) {
            smnt = "INSERT INTO job (name, state, departure_date, flight_number, prior_to_reg) VALUES (?, ?, ?, ?, ?);";
        }

        try {
            PreparedStatement statement = this.connection.prepareStatement(smnt);
            statement.setString(1, job.getName());
            statement.setString(2, job.getState().toString());
            statement.setString(3, String.valueOf(job.getDepartureDateTimestamp()));
            statement.setString(4, job.getFlightNumber());
            statement.setInt(5, job.getPriorToReg());

            if (statement.executeUpdate() == 0) {
                logger.warning("Save job error");
            }

            if (savedJob == null) {
                int id = statement.getGeneratedKeys().getInt(1);
                job.getTickets().forEach(ticket -> ticket.setJobId(id));
                job.setId(id);
            }

            job.getTickets().forEach(this::saveTicket);

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create statement", ex);
        }
    }

    public Ticket getTicket(int id) {

        if (id == 0) {
            return null;
        }

        try {
            Statement statement = this.connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM ticket WHERE id = " + id);
            rs.next();

            return new Ticket(id, rs.getString("last_name"), rs.getString("number"), rs.getInt("job_id"));

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create statement", ex);
        }

        return null;
    }

    /**
     * Сохранение или добавление нового билета в БД.
     *
     * @param ticket новый или изменённый билет
     */
    public synchronized void saveTicket(Ticket ticket) {

        String smnt = "UPDATE ticket SET (last_name, number, job_id) = (?, ?, ?) WHERE id = " + ticket.getId() + ";";

        Ticket savedTicket = getTicket(ticket.getId());

        if (savedTicket == null) {
            smnt = "INSERT INTO ticket (last_name, number, job_id) VALUES (?, ?, ?);";
        }

        try {
            PreparedStatement statement = this.connection.prepareStatement(smnt);
            statement.setString(1, ticket.getLastName());
            statement.setString(2, ticket.getNumber());
            statement.setInt(3, ticket.getJobId());

            if (statement.executeUpdate() == 0) {
                logger.warning("Save ticket error");
                return;
            }

            if (savedTicket == null) {
                int id = statement.getGeneratedKeys().getInt(1);
                ticket.setId(id);
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create statement", ex);
        }
    }

    /**
     * Удаление билета из БД.
     *
     * @param id
     */
    public synchronized void deleteTicket(int id) {

        if (id == 0) {
            return;
        }

        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate("DELETE FROM ticket WHERE id = " + id);

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to delete ticket", ex);
        }
    }

    public List<Airplane> getAirplanes() {
        List<Airplane> airplanes = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM airplane;");

            while (rs.next()) {
                airplanes.add(new Airplane(rs.getInt(1), rs.getString("name")));
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get airplanes: ", ex);
        }

        return airplanes;
    }
}
