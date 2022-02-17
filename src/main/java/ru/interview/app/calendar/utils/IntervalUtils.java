package ru.interview.app.calendar.utils;

import org.springframework.data.util.Pair;

import java.util.List;

public class IntervalUtils {


    /**
     * @param intervals              - busy intervals sorted by start time (no duplication of start time present).
     * @param startInterval          - start of the searching segment.
     * @param endInterval            - end of the searching segment.
     * @param intervalLengthToSearch - length of interval to search.
     * @return - found free interval with min start time and max end time.
     */
    public static Pair<Long, Long> findFirstFreeInterval(List<Pair<Long, Long>> intervals,
                                                         long startInterval, long endInterval, long intervalLengthToSearch) {

        Pair<Long, Long> result = null;
        long freePeriodStart = startInterval;

        for (Pair<Long, Long> interval : intervals) {
            long startIntervalTime = interval.getFirst();

            if (freePeriodStart + intervalLengthToSearch <= startIntervalTime) {
                result = Pair.of(freePeriodStart, startIntervalTime);
                break;
            } else {
                freePeriodStart = interval.getSecond();
            }
        }

        if (result == null && (freePeriodStart + intervalLengthToSearch) <= endInterval) {
            return Pair.of(freePeriodStart, endInterval);
        }

        return result;
    }
}
