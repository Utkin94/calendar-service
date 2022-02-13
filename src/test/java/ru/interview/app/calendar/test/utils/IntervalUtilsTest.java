package ru.interview.app.calendar.test.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.util.Pair.of;
import static ru.interview.app.calendar.utils.IntervalUtils.findFirstFreeInterval;

public class IntervalUtilsTest {

    @Test
    public void findFirstFreeIntervalInMiddleTest() {
        var foundInterval = findFirstFreeInterval(
                List.of(
                        of(1L, 3L),
                        of(4L, 6L),
                        of(6L, 10L),
                        of(13L, 15L)
                ),
                0,
                15,
                3
        );

        assertThat(foundInterval)
                .matches(interval -> interval.getFirst() == 10)
                .matches(interval -> interval.getSecond() == 13);
    }


    @Test
    public void findFirstFreeInterval_shouldReturnEndOfLastMeetingAsStartIntervalAndEndSearchingIntervalAsEndInterval() {
        var foundInterval = findFirstFreeInterval(
                List.of(
                        of(1L, 3L),
                        of(2L, 4L)
                ),
                0,
                15,
                2
        );

        assertThat(foundInterval)
                .matches(interval -> interval.getFirst() == 4)
                .matches(interval -> interval.getSecond() == 15);
    }

    @Test
    public void findFirstFreeInterval_shouldReturnIntervalFromStartToFirstIntervalStart() {
        var foundInterval = findFirstFreeInterval(
                List.of(
                        of(1L, 3L),
                        of(2L, 4L)
                ),
                0,
                15,
                1
        );

        assertThat(foundInterval)
                .matches(interval -> interval.getFirst() == 0)
                .matches(interval -> interval.getSecond() == 1);
    }

    @Test
    public void findFirstFreeInterval_shouldReturnnull() {
        var foundInterval = findFirstFreeInterval(
                List.of(
                        of(1L, 3L),
                        of(2L, 4L)
                ),
                0,
                4,
                2
        );

        assertThat(foundInterval).isNull();
    }


    @Test
    public void findFirstFreeInterval_shouldReturnWholeInterval() {
        var foundInterval = findFirstFreeInterval(
                List.of(),
                0,
                4,
                2
        );

        assertThat(foundInterval)
                .matches(interval -> interval.getFirst() == 0)
                .matches(interval -> interval.getSecond() == 4);
    }
}
