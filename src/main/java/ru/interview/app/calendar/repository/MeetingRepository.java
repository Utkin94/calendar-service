package ru.interview.app.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.interview.app.calendar.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
