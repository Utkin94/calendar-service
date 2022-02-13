package ru.interview.app.calendar.test.service;

import org.junit.jupiter.api.Test;
import ru.interview.app.calendar.dto.Interval;
import ru.interview.app.calendar.service.impl.IntervalServiceImpl;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IntervalServiceTest {

    @Test
    public void findIntervalFor3HoursMeeting_intervalShouldBeFound() {
        var intervalService = new IntervalServiceImpl();

        var lowerBound = ZonedDateTime.parse("2007-12-03T00:00:00Z");

        var meetingIntervals = List.of(
                new Interval(ZonedDateTime.parse("2007-12-03T01:00:00Z"), ZonedDateTime.parse("2007-12-03T03:00:00Z")),
                new Interval(ZonedDateTime.parse("2007-12-03T04:00:00Z"), ZonedDateTime.parse("2007-12-03T06:00:00Z")),
                new Interval(ZonedDateTime.parse("2007-12-03T06:00:00Z"), ZonedDateTime.parse("2007-12-03T07:00:00Z")),
                new Interval(ZonedDateTime.parse("2007-12-03T06:00:00Z"), ZonedDateTime.parse("2007-12-03T10:00:00Z")),
                new Interval(ZonedDateTime.parse("2007-12-03T13:00:00Z"), ZonedDateTime.parse("2007-12-03T15:00:00Z"))
        );

        var upperBound = ZonedDateTime.parse("2007-12-03T18:00:00Z");

        var searchingInteval = new Interval(lowerBound, upperBound);

        var expectedStartTime = ZonedDateTime.parse("2007-12-03T10:00:00Z");
        var expectedEndTime = ZonedDateTime.parse("2007-12-03T13:00:00Z");

        assertThat(intervalService.findFreeIntervalOfLength(meetingIntervals, searchingInteval, 180))
                .matches(meetingInterval -> meetingInterval.getStarTime().isEqual(expectedStartTime))
                .matches(meetingInterval -> meetingInterval.getEndTime().isEqual(expectedEndTime));
    }
}
