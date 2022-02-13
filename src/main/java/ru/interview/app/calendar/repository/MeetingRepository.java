package ru.interview.app.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.interview.app.calendar.dto.Interval;
import ru.interview.app.calendar.entity.Meeting;

import java.time.ZonedDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("select m from Meeting m join m.members mm " +
            "where m.status = 'OPEN' and mm.id.userId = :userId and m.endTime >= :from and m.startTime <= :to")
    List<Meeting> findAllByMemberMeetingOnPeriod(long userId, ZonedDateTime from, ZonedDateTime to);

    @Query(value = "select new ru.interview.app.calendar.dto.Interval(m.startTime, m.endTime) from Meeting m join m.members mm " +
            "where m.startTime <= :upperBound and mm.id.userId in (:userIds) " +
            "and mm.memberStatus = 'ACCEPTED' and m.status <> 'CLOSED'")
    List<Interval> findMeetingsIntervalsByUserIds(List<Long> userIds, ZonedDateTime upperBound);
}
