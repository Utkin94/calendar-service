package ru.interview.app.calendar.service.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import ru.interview.app.calendar.service.MeetingMemberNotifier;

import static ru.interview.app.calendar.service.impl.NotificationCreatorServiceImpl.MEETING_ID_JOB_DATA_KEY;

public abstract class AbstractMeetingNotifyJob extends QuartzJobBean {

    @Autowired
    private MeetingMemberNotifier notifyService;

    protected abstract NotificationType getNotifyType();

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        var meetingId = (Long) context.getMergedJobDataMap().get(MEETING_ID_JOB_DATA_KEY);
        notifyService.notifyMembers(meetingId, getNotifyType());
    }
}
