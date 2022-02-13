package ru.interview.app.calendar.service;

import ru.interview.app.calendar.dto.Interval;

import java.util.List;

public interface IntervalService {

    /**
     * Find free interval of specified length.
     *
     * @param meetingIntervals      - intervals those are occupied.
     * @param searchingInterval     - searching interval.
     * @param minFreeIntervalToFind - length of free interval to find.
     * @return the whole free interval with minimum startTime. Can be null if there's no free interval
     * @throws IllegalArgumentException if free interval cannot be found.
     */
    Interval findFreeIntervalOfLength(List<Interval> meetingIntervals, Interval searchingInterval, int minFreeIntervalToFind) throws IllegalArgumentException;
}
