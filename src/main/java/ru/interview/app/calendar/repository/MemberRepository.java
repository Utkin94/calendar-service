package ru.interview.app.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.UserMeetingKey;

public interface MemberRepository extends JpaRepository<MeetingMember, UserMeetingKey> {
}
