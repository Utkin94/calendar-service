package ru.interview.app.calendar.service.impl;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.interview.app.calendar.dto.Interval;
import ru.interview.app.calendar.service.IntervalService;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.BinaryOperator.maxBy;
import static ru.interview.app.calendar.utils.IntervalUtils.findFirstFreeInterval;
import static ru.interview.app.calendar.utils.TimeUtils.epochMillisToZonedDateTime;

@Service
public class IntervalServiceImpl implements IntervalService {

    @Override
    public Interval findFreeIntervalOfLength(List<Interval> meetingIntervals,
                                             Interval searchingInterval, int minFreeIntervalToFind) {

        var meetingTimeToFindImMillis = TimeUnit.MINUTES.toMillis(minFreeIntervalToFind);
        var searchingStartTime = searchingInterval.getStarTime().toInstant().toEpochMilli();
        var searchingEndTime = searchingInterval.getEndTime().toInstant().toEpochMilli();

        var foundInterval = findFirstFreeInterval(mapIntervalsToSortedIntervalPairs(meetingIntervals),
                searchingStartTime, searchingEndTime, meetingTimeToFindImMillis);

        if (foundInterval != null) {
            return mapIntervalPairToInterval(foundInterval);
        }

        throw new IllegalArgumentException("Free interval for specified users not found");
    }

    private Interval mapIntervalPairToInterval(Pair<Long, Long> foundInterval) {
        return new Interval(epochMillisToZonedDateTime(foundInterval.getFirst()), epochMillisToZonedDateTime(foundInterval.getSecond()));
    }

    private List<Pair<Long, Long>> mapIntervalsToSortedIntervalPairs(List<Interval> meetingIntervals) {
        var startTimeToIntervalPairWithMaxEndTime = meetingIntervals.stream()
                .map(this::mapIntervalToIntervalLongPair)
                .collect(Collectors.toMap(
                        Pair::getFirst,
                        Function.identity(),
                        maxBy(Comparator.comparingLong(Pair::getSecond)))
                );

        return startTimeToIntervalPairWithMaxEndTime.values().stream()
                .sorted(Comparator.comparingLong(Pair::getFirst))
                .collect(Collectors.toList());
    }

    private Pair<Long, Long> mapIntervalToIntervalLongPair(Interval interval) {
        return Pair.of(interval.getStarTime().toInstant().toEpochMilli(), interval.getEndTime().toInstant().toEpochMilli());
    }
}
