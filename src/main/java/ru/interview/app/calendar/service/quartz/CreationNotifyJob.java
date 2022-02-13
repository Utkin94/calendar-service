package ru.interview.app.calendar.service.quartz;

public class CreationNotifyJob extends AbstractMeetingNotifyJob {
    @Override
    protected NotificationType getNotifyType() {
        return NotificationType.CREATION;
    }
}
