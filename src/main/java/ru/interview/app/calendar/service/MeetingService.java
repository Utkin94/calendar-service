package ru.interview.app.calendar.service;

import ru.interview.app.calendar.dto.request.AvailableTimeQuery;
import ru.interview.app.calendar.dto.request.MeetingCreate;
import ru.interview.app.calendar.dto.response.AvailableTimeResponse;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MemberStatus;

import java.time.ZonedDateTime;
import java.util.List;

public interface MeetingService {

    Meeting createMeeting(MeetingCreate meetingCreate);

    Meeting getMeeting(long meetingId);

    Meeting changeMemberStatus(long meetingId, long userId, MemberStatus status);

    List<Meeting> getMemberMeetingsByPeriod(long userId, ZonedDateTime from, ZonedDateTime to);

    AvailableTimeResponse getAvailableTime(AvailableTimeQuery query);
}
