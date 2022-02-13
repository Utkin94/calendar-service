package ru.interview.app.calendar.service.quartz;

public class MeetingNotifyBeforeHourJob extends AbstractMeetingNotifyJob {
    @Override
    protected NotificationType getNotifyType() {
        return NotificationType.BEFORE_START;
    }
}
