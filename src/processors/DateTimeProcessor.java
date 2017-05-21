package processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Сервис точного времени
 *
 * Created by Andrey Semenyuk on 2017.
 */
public class DateTimeProcessor {

    // Адрес сервиса точного времени
    private final static String timeUrl = "https://time100.ru/api.php";

    /**
     * Получает текущее время в секундах из сервиса точного времени, если не удаётся - берём системное
     *
     * @return long
     */
    public static long getCurrentSecond() {
        URL url;

        try {
            url = new URL(timeUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return Long.valueOf(br.readLine());
        } catch (IOException ex) {
            return System.currentTimeMillis() / 1000;
        }
    }

    /**
     * Получает текущие дату и время из сервиса точного времени, если не удаётся - берём системное
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(getCurrentSecond()), ZoneId.of("GMT+5"));
    }
}
