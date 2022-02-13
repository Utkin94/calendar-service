package ru.interview.app.calendar.service;

import ru.interview.app.calendar.controller.dto.request.MeetingCreate;
import ru.interview.app.calendar.entity.Meeting;

public interface MeetingService {

    Meeting createMeeting(MeetingCreate meetingCreate);

}
