package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.dto.Interval;
import ru.interview.app.calendar.dto.request.AvailableTimeQuery;
import ru.interview.app.calendar.dto.request.MeetingCreate;
import ru.interview.app.calendar.dto.response.AvailableTimeResponse;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.MemberMeetingId;
import ru.interview.app.calendar.entity.MemberStatus;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.IntervalService;
import ru.interview.app.calendar.service.MeetingService;
import ru.interview.app.calendar.service.NotificationCreatorService;
import ru.interview.app.calendar.service.ValidationService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final IntervalService intervalService;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final NotificationCreatorService notificationCreatorService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Meeting createMeeting(MeetingCreate meetingCreate) {
        validateMeetingCreate(meetingCreate);

        var userCreator = userRepository.findById(meetingCreate.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("There's no user with id " + meetingCreate.getCreatorId()));

        var meetingToCreate = new Meeting();
        meetingToCreate.setCreator(userCreator);
        meetingToCreate.setStatus(meetingCreate.getStatus());
        meetingToCreate.setTitle(meetingCreate.getTitle());
        meetingToCreate.setStartTime(meetingCreate.getMeetingStartTime());
        meetingToCreate.setEndTime(meetingCreate.getMeetingEndTime());
        meetingToCreate.setMembers(createMeetingMemberList(meetingCreate.getMemberUserIds(), meetingToCreate));

        var savedMeeting = meetingRepository.save(meetingToCreate);
        notificationCreatorService.createMeetingMembersNotifications(savedMeeting.getId(), savedMeeting.getStartTime());
        return savedMeeting;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Meeting getMeeting(long meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Meeting changeMemberStatus(long meetingId, long userId, MemberStatus status) {
        var meeting = meetingRepository.findById(meetingId).orElseThrow(IllegalArgumentException::new);

        var meetingMember = meeting.getMembers()
                .stream()
                .filter(member -> member.getId().getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not meeting's member"));

        meetingMember.setMemberStatus(status);

        return meetingRepository.save(meeting);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Meeting> getMemberMeetingsByPeriod(long userId, ZonedDateTime from, ZonedDateTime to) {
        return meetingRepository.findAllByMemberMeetingOnPeriod(userId, from, to);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AvailableTimeResponse getAvailableTime(AvailableTimeQuery query) {
        validationService.validate(query);
        var searchingStartTime = ZonedDateTime.now();
        var searchingEndTime = searchingStartTime.plusMonths(1);

        var intervals = meetingRepository.findMeetingsIntervalsByUserIds(query.getUserIds(), searchingEndTime);

        var foundInterval = intervalService.findFreeIntervalOfLength(
                intervals, new Interval(searchingStartTime, searchingEndTime), query.getMinFreeIntervalToFind());

        return new AvailableTimeResponse(foundInterval.getStarTime(), foundInterval.getEndTime());
    }

    private List<MeetingMember> createMeetingMemberList(Set<Long> memberIds, Meeting savedMeeting) {
        var memberUsers = userRepository.findAllById(memberIds);
        if (memberUsers.size() != memberIds.size()) {
            throw new IllegalArgumentException("There are no users with ids: " + getNotExistedUsersIds(memberIds, memberUsers));
        }

        return memberUsers
                .stream()
                .map(user -> createMeetingMember(savedMeeting, user))
                .collect(Collectors.toList());
    }

    private MeetingMember createMeetingMember(Meeting savedMeeting, User user) {
        var meetingMember = new MeetingMember();
        meetingMember.setId(new MemberMeetingId(user.getId(), savedMeeting.getId()));
        meetingMember.setUser(user);
        meetingMember.setMeeting(savedMeeting);
        meetingMember.setMemberStatus(MemberStatus.NONE);

        return meetingMember;
    }

    private void validateMeetingCreate(MeetingCreate meetingCreate) {
        validationService.validate(meetingCreate);
        if (meetingCreate.getMeetingEndTime().isBefore(meetingCreate.getMeetingStartTime())) {
            throw new IllegalArgumentException("Incorrect start and end times: endTime < startTime");
        }
    }

    private List<Long> getNotExistedUsersIds(Set<Long> memberUserIds, List<User> memberUsers) {
        var notExistedUsersIds = new HashSet<>(memberUserIds);
        memberUsers.forEach(memberUser -> notExistedUsersIds.remove(memberUser.getId()));

        return new ArrayList<>(notExistedUsersIds);
    }
}
