package ru.interview.app.calendar.utils;


import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    private static final long QUARTER_HOUR_MILLIS = Duration.of(15, ChronoUnit.MINUTES).toMillis();

    public static boolean isHourQuarter(ZonedDateTime dateTime) {
        boolean isNotHourQuarter = dateTime.getSecond() != 0 || dateTime.getNano() != 0 || dateTime.getMinute() % 15 != 0;
        return !isNotHourQuarter;
    }

    public static ZonedDateTime roundUpToQuarterHour(ZonedDateTime zonedDateTime) {
        var startOfDay = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        long millisSinceStartOfDay = zonedDateTime.toInstant().toEpochMilli() - startOfDay.toInstant().toEpochMilli();

        long hourQuartersCount = millisSinceStartOfDay / QUARTER_HOUR_MILLIS;
        if (millisSinceStartOfDay % QUARTER_HOUR_MILLIS > 0) {
            hourQuartersCount++;
        }

        return startOfDay.plus(hourQuartersCount * 15, ChronoUnit.MINUTES);
    }

    public static ZonedDateTime epochMillisToZonedDateTime(Long epochMillis) {
        var instant = Instant.ofEpochMilli(epochMillis);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
