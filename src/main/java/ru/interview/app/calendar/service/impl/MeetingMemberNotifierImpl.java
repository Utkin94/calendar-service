package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.service.MeetingMemberNotifier;
import ru.interview.app.calendar.service.quartz.NotificationType;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingMemberNotifierImpl implements MeetingMemberNotifier {

    private final MeetingRepository meetingRepository;

    @Override
    public void notifyMembers(Long meetingId, NotificationType creation) {
        var meeting = meetingRepository.findById(meetingId).orElseThrow();

        var membersIds = meeting.getMembers().stream()
                .map(member -> member.getId().getUserId())
                .collect(Collectors.toList());

        for (Long memberId : membersIds) {
            if (creation == NotificationType.CREATION) {
                log.info("Notify user {} that meeting {} was created", memberId, meetingId);
            } else {
                log.info("Notify user {} that meeting {} starts in 1 hour", memberId, meetingId);
            }
        }
    }
}
