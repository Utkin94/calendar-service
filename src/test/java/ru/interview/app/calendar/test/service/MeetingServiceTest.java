package ru.interview.app.calendar.test.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.interview.app.calendar.controller.dto.request.MeetingCreate;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.repository.MemberRepository;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.MeetingService;
import ru.interview.app.calendar.service.impl.MeetingServiceImpl;

import javax.validation.Validator;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeetingServiceTest {

    private MeetingService meetingService;
    private UserRepository userRepository;
    private MemberRepository memberRepository;
    private MeetingRepository meetingRepository;

    @BeforeAll
    public void init(@Mock MeetingRepository meetingRepository,
                     @Mock UserRepository userRepository,
                     @Mock MemberRepository memberRepository,
                     @Mock Validator validator) {
        this.meetingService = new MeetingServiceImpl(meetingRepository, memberRepository, userRepository, validator);
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.meetingRepository = meetingRepository;
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(userRepository, meetingRepository);
    }

    @Test
    public void callCreateMeeting_meetingRepositoryShouldBeCalledWithExpectedParamAndExpectedResultShouldBeReturned() {
        var creatorId = 1L;
        var meetingTitle = "title";
        var meetingStartTime = ZonedDateTime.now().plusHours(1);
        var meetingEndTime = meetingStartTime.plusHours(1);
        var memberUserIds = Set.of(2L, 3L);

        var meetingCreate = new MeetingCreate()
                .setCreatorId(creatorId)
                .setTitle(meetingTitle)
                .setMeetingStartTime(meetingStartTime)
                .setMeetingEndTime(meetingEndTime)
                .setMemberUserIds(memberUserIds);


        var expectedCreator = new User().setId(creatorId).setFirstName("first").setLastName("last");
        var expectedMeetingToCreate = new Meeting()
                .setTitle(meetingTitle)
                .setStartTime(meetingStartTime)
                .setEndTime(meetingEndTime)
                .setCreator(expectedCreator);

        expectedMeetingToCreate.setMembers(List.of(
                new MeetingMember().setUser(new User().setId(2L)).setMeeting(expectedMeetingToCreate),
                new MeetingMember().setUser(new User().setId(3L)).setMeeting(expectedMeetingToCreate)
        ));

        var expectedCreationResult = new Meeting()
                .setId(777L)
                .setCreator(expectedCreator)
                .setStartTime(meetingStartTime)
                .setEndTime(meetingEndTime)
                .setTitle(meetingTitle)
                .setMembers(expectedMeetingToCreate.getMembers());

        when(userRepository.findAllById(memberUserIds)).thenReturn(List.of(new User().setId(2L), new User().setId(3L)));
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(expectedCreator));
        when(memberRepository.save(any())).thenReturn(expectedMeetingToCreate.getMembers());
        when(meetingRepository.save(argThat(meeting -> meeting.getCreator().equals(expectedCreator))))
                .thenReturn(expectedCreationResult);

        assertThat(meetingService.createMeeting(meetingCreate))
                .matches(meeting -> meeting.getTitle().equals(expectedCreationResult.getTitle()))
                .matches(meeting -> meeting.getId().equals(expectedCreationResult.getId()))
                .matches(meeting -> meeting.getStartTime().isEqual(expectedCreationResult.getStartTime()))
                .matches(meeting -> meeting.getEndTime().isEqual(expectedCreationResult.getEndTime()))
                .matches(meeting -> meeting.getCreator().equals(expectedCreationResult.getCreator()))
                .matches(meeting -> meeting.getMembers().equals(expectedCreationResult.getMembers()));
    }

    @Test
    public void tryToCreateMeetingWithNonExistingCreatorId_shouldThrowIllegalArgumentException() {
        var meetingCreate = new MeetingCreate()
                .setTitle("title")
                .setCreatorId(1L)
                .setMeetingStartTime(ZonedDateTime.now())
                .setMeetingEndTime(ZonedDateTime.now().plusHours(1))
                .setMemberUserIds(Set.of(2L, 3L));
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(meetingCreate));
    }

    @Test
    public void tryToCreateMeetingWithNonExistingUserAsAMember_shouldThrowIllegalArgumentException() {
        var meetingCreate = new MeetingCreate()
                .setTitle("title")
                .setCreatorId(1L)
                .setMeetingStartTime(ZonedDateTime.now())
                .setMeetingEndTime(ZonedDateTime.now().plusHours(1))
                .setMemberUserIds(Set.of(2L, 3L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User().setId(1L).setFirstName("first").setLastName("last")));

        when(userRepository.findAllById(meetingCreate.getMemberUserIds())).thenReturn(List.of(new User().setId(2L)));

        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(meetingCreate));
    }
}
