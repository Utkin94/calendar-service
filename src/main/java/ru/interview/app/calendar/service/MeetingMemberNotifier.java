package ru.interview.app.calendar.service;

import ru.interview.app.calendar.service.quartz.NotificationType;

public interface MeetingMemberNotifier {
    void notifyMembers(Long meetingId, NotificationType creation);
}
