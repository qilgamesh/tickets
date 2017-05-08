package handlers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Job;
import model.Ticket;
import org.sqlite.JDBC;
import utils.LogUtils;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
                            "'departure_date' TIMESTAMP);"
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
                            "'flight_number' TEXT," +
                            "'job_id' INTEGER REFERENCES job (id));"
            );
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create tables: ", ex);
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
            // получяем только активные задания
            // ResultSet rs = statement.executeQuery("SELECT id, name, execute_date, state FROM main.jobs WHERE state = 'active';");
            // TODO для теста !! удалить после теста
            ResultSet rs = statement.executeQuery("SELECT * FROM job;");
            Random random = new Random();

            while (rs.next()) {
                Job job = new Job(rs.getInt("id"), rs.getString("name"), rs.getString("state"), rs.getString("departure_date"));
                job.setTickets(getTicketByJobId(job.getId()));
                // TODO для теста !! удалить после теста
                int rnd = random.nextInt(30);
                job.setDepartureDate(LocalDateTime.now().plusSeconds(3610 + rnd).toString());
                job.setState("NEW");
                job.setName("JOB#" + rnd);
                jobs.add(job);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get jobs: ", ex);
        }

        return jobs;
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
                tickets.add(new Ticket(rs.getInt("id"), rs.getString("last_name"), rs.getString("number"), rs.getString("date"), rs.getString("flight_number"), id));
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
            logger.info("Result select job: " + rs.getRow());
            return new Job(id, rs.getString(2), rs.getString(3), rs.getString(4));
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
        String smnt = "UPDATE job SET (name, state, departure_date) = (?, ?, ?) WHERE id = " + job.getId() + ";";

        if (savedJob == null) {
            smnt = "INSERT INTO job (name, state, departure_date) VALUES (?, ?, ?);";
            logger.info("Save new job with name: " + job.getName());
        } else {
            logger.info("Update saved job with name: " + savedJob.getName());
        }

        try {
            PreparedStatement statement = this.connection.prepareStatement(smnt);
            statement.setString(1, job.getName());
            statement.setString(2, job.getState());
            statement.setString(3, String.valueOf(job.getDepartureDateTimestamp()));

            if (statement.executeUpdate() == 0) {
                logger.warning("Save job error");
            }

            if (savedJob == null) {
                int id = statement.getGeneratedKeys().getInt(1);
                logger.info("New job successful saved (id: " + id + ")");
                job.getTickets().forEach(ticket -> ticket.setJobId(id));
            } else {
                logger.info("Job successful updated (id=" + job.getId() + ")");
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

            return new Ticket(id, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6));

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

        String smnt = "UPDATE ticket SET (last_name, number, date, flight_number, job_id) = (?, ?, ?, ?, ?) WHERE id = " + ticket.getId() + ";";

        Ticket savedTicket = getTicket(ticket.getId());

        if (savedTicket == null) {
            smnt = "INSERT INTO ticket (last_name, number, date, flight_number, job_id) VALUES (?, ?, ?, ?, ?);";
            logger.info("Save new ticket for job id: " + ticket.getJobId());
        } else {
            logger.info("Update saved ticket for job id: " + savedTicket.getJobId());
        }

        try {
            PreparedStatement statement = this.connection.prepareStatement(smnt);
            statement.setString(1, ticket.getLastName());
            statement.setString(2, ticket.getNumber());
            statement.setString(3, ticket.getDate());
            statement.setString(4, ticket.getFlightNumber());
            statement.setInt(5, ticket.getJobId());

            if (statement.executeUpdate() == 0) {
                logger.warning("Save ticket error");
                return;
            }

            if (savedTicket == null) {
                int id = statement.getGeneratedKeys().getInt(1);
                logger.info("New ticket successful saved (id: " + id + ")");
            } else {
                logger.info("Ticket successful updated (id=" + ticket.getId() + ")");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create statement", ex);
        }
    }
}
