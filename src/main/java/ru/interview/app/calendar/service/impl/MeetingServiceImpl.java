package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.controller.dto.request.MeetingCreate;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.entity.UserMeetingKey;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.repository.MemberRepository;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.MeetingService;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final Validator validator;

    @Override
    @Transactional
    public Meeting createMeeting(MeetingCreate meetingCreate) {
        validateMeetingCreate(meetingCreate);

        var userCreator = userRepository.findById(meetingCreate.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("There's no user with id " + meetingCreate.getCreatorId()));

        var meetingToCreate = new Meeting();
        meetingToCreate.setCreator(userCreator);
        meetingToCreate.setTitle(meetingCreate.getTitle());
        meetingToCreate.setStartTime(meetingCreate.getMeetingStartTime());
        meetingToCreate.setEndTime(meetingCreate.getMeetingEndTime());

        var savedMeeting = meetingRepository.save(meetingToCreate);
        savedMeeting.setMembers(createMeetingMemberList(meetingCreate.getMemberUserIds(), savedMeeting));

        return savedMeeting;
    }

    private List<MeetingMember> createMeetingMemberList(Set<Long> memberIds, Meeting savedMeeting) {
        var memberUsers = userRepository.findAllById(memberIds);
        if (memberUsers.size() != memberIds.size()) {
            throw new IllegalArgumentException("There are no users with ids: " + getNotExistedUsersIds(memberIds, memberUsers));
        }

        var meetingMembers = memberUsers
                .stream()
                .map(user -> createMeetingMember(savedMeeting, user))
                .collect(Collectors.toList());

        return memberRepository.saveAll(meetingMembers);
    }

    private MeetingMember createMeetingMember(Meeting savedMeeting, User user) {
        var meetingMember = new MeetingMember();
        meetingMember.setId(
                new UserMeetingKey()
                        .setUserId(user.getId())
                        .setMeetingId(savedMeeting.getId())
        );
        meetingMember.setUser(user);
        meetingMember.setMeeting(savedMeeting);
        meetingMember.setInvitationAccepted(false);

        return meetingMember;
    }

    private void validateMeetingCreate(MeetingCreate meetingCreate) {
        var violations = validator.validate(meetingCreate);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var constraintViolation : violations) {
                sb.append(constraintViolation.getPropertyPath().toString())
                        .append(" ")
                        .append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

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
