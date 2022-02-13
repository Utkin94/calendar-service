package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import ru.interview.app.calendar.service.NotificationCreatorService;
import ru.interview.app.calendar.service.quartz.CreationNotifyJob;
import ru.interview.app.calendar.service.quartz.MeetingNotifyBeforeHourJob;
import ru.interview.app.calendar.service.quartz.NotificationType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCreatorServiceImpl implements NotificationCreatorService {

    public static final String MEETING_ID_JOB_DATA_KEY = "meetingId";

    private final Scheduler scheduler;

    @Override
    public void createMeetingMembersNotifications(long meetingId, ZonedDateTime startTime) {
        createMeetingCreationNotification(meetingId);
        createMeetingBeforeStartNotification(meetingId, startTime);
    }

    @SneakyThrows
    private void createMeetingBeforeStartNotification(long meetingId, ZonedDateTime startTime) {
        var jobDetail = JobBuilder.newJob()
                .ofType(MeetingNotifyBeforeHourJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .usingJobData(MEETING_ID_JOB_DATA_KEY, meetingId)
                .build();

        var trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(NotificationType.BEFORE_START.name())
                .startAt(meetingStartTimeToTriggerBeforeOneHour(startTime))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    @SneakyThrows
    private void createMeetingCreationNotification(long meetingId) {
        var jobDetail = JobBuilder.newJob()
                .ofType(CreationNotifyJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .usingJobData(MEETING_ID_JOB_DATA_KEY, meetingId)
                .build();

        var trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(NotificationType.CREATION.name())
                .startAt(Date.from(Instant.now().plus(10, ChronoUnit.SECONDS)))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private Date meetingStartTimeToTriggerBeforeOneHour(ZonedDateTime meetingStartTime) {
        var instant = meetingStartTime.withZoneSameInstant(ZoneId.systemDefault()).minusHours(1).toInstant();
        return Date.from(instant);
    }
}
