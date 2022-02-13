package ru.interview.app.calendar.service;

import java.time.ZonedDateTime;

public interface NotificationCreatorService {
    void createMeetingMembersNotifications(long meetingId, ZonedDateTime startTime);
}
