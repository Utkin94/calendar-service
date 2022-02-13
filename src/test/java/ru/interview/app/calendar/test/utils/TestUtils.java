package ru.interview.app.calendar.test.utils;

import org.hamcrest.Matcher;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;

public class TestUtils {

    public static Matcher<String> isDate(ZonedDateTime dateTime) {
        return is(dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
