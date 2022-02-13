package ru.interview.app.calendar.test.utils;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.interview.app.calendar.utils.TimeUtils.epochMillisToZonedDateTime;
import static ru.interview.app.calendar.utils.TimeUtils.isHourQuarter;
import static ru.interview.app.calendar.utils.TimeUtils.roundUpToQuarterHour;

public class TimeUtilsTest {

    @Test
    public void roundUpToQuarterHourTest() {
        assertTrue(ZonedDateTime.parse("2007-12-03T10:30:00+01:00[Europe/Paris]").isEqual(roundUpToQuarterHour(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"))));
        assertTrue(ZonedDateTime.parse("2007-12-03T10:15:00+01:00[Europe/Paris]").isEqual(roundUpToQuarterHour(ZonedDateTime.parse("2007-12-03T10:15:00+01:00[Europe/Paris]"))));
        assertTrue(ZonedDateTime.parse("2007-12-03T10:15:00+01:00[Europe/Paris]").isEqual(roundUpToQuarterHour(ZonedDateTime.parse("2007-12-03T10:15:00+01:00[Europe/Paris]"))));
        assertTrue(ZonedDateTime.parse("2007-12-03T10:30:00+01:00[Europe/Paris]").isEqual(roundUpToQuarterHour(ZonedDateTime.parse("2007-12-03T10:15:01+01:00[Europe/Paris]"))));
        assertTrue(ZonedDateTime.parse("2007-12-04T00:00:00+01:00[Europe/Paris]").isEqual(roundUpToQuarterHour(ZonedDateTime.parse("2007-12-03T23:55:00+01:00[Europe/Paris]"))));
    }

    @Test
    public void epochMillisToZonedDateTimeTest() {
        var time = ZonedDateTime.parse("2007-12-03T10:30:00+01:00[Europe/Paris]");
        var epochMilli = time.toInstant().toEpochMilli();

        assertTrue(epochMillisToZonedDateTime(epochMilli).isEqual(time));
    }

    @Test
    public void isNotHourQuarterTest() {
        assertFalse(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:31:01+01:00[Europe/Paris]")));
        assertFalse(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:30:01+01:00[Europe/Paris]")));
        assertFalse(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:30:00.000000001+01:00[Europe/Paris]")));
        assertFalse(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:35:00+01:00[Europe/Paris]")));
        assertFalse(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:40:00+01:00[Europe/Paris]")));

        assertTrue(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:00:00+01:00[Europe/Paris]")));
        assertTrue(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:15:00+01:00[Europe/Paris]")));
        assertTrue(isHourQuarter(ZonedDateTime.parse("2007-12-03T10:30:00+01:00[Europe/Paris]")));
    }
}
