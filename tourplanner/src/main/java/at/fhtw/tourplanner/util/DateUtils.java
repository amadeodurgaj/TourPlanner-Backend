package at.fhtw.tourplanner.util;

import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
}