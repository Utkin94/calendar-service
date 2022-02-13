package ru.interview.app.calendar.test.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.interview.app.calendar.dto.Interval;
import ru.interview.app.calendar.dto.request.AvailableTimeQuery;
import ru.interview.app.calendar.dto.request.MeetingCreate;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.MemberMeetingId;
import ru.interview.app.calendar.entity.MemberStatus;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.MeetingService;
import ru.interview.app.calendar.service.NotificationCreatorService;
import ru.interview.app.calendar.service.ValidationService;
import ru.interview.app.calendar.service.impl.IntervalServiceImpl;
import ru.interview.app.calendar.service.impl.MeetingServiceImpl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeetingServiceTest {

    private MeetingService meetingService;
    private UserRepository userRepository;
    private MeetingRepository meetingRepository;
    private NotificationCreatorService notificationCreatorService;

    @BeforeAll
    public void init(@Mock MeetingRepository meetingRepository,
                     @Mock UserRepository userRepository,
                     @Mock ValidationService validator,
                     @Mock NotificationCreatorService notificationCreatorService
    ) {
        this.meetingService = new MeetingServiceImpl(new IntervalServiceImpl(), meetingRepository, userRepository, validator, notificationCreatorService);
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
        this.notificationCreatorService = notificationCreatorService;
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(userRepository, meetingRepository, meetingRepository);
    }

    @Test
    public void getAvailableTime_availableTimeShouldBeReturned() {
        var query = new AvailableTimeQuery()
                .setMinFreeIntervalToFind(60)
                .setUserIds(List.of(1L, 2L, 3L));

        var startTestTime = ZonedDateTime.now();
        var validStartIntervalTime = startTestTime.plusHours(5);
        var upperBound = startTestTime.plusMonths(1);

        when(meetingRepository.findMeetingsIntervalsByUserIds(any(), any())).thenReturn(List.of(
                new Interval(startTestTime, validStartIntervalTime)
        ));

        assertThat(meetingService.getAvailableTime(query))
                .matches(response -> response.getStartFreeInterval().truncatedTo(ChronoUnit.MINUTES)
                        .isEqual(validStartIntervalTime.truncatedTo(ChronoUnit.MINUTES)))
                .matches(response -> response.getEndFreeInterval().truncatedTo(ChronoUnit.MINUTES)
                        .isEqual(upperBound.truncatedTo(ChronoUnit.MINUTES)));
    }

    @Test
    public void getMemberMeetingsByPeriod_memberMeetingsShouldBeReturned() {
        var userId = 1L;
        var from = ZonedDateTime.now();
        var to = ZonedDateTime.now();

        var firstMeetingId = 1L;
        var secondMeetingId = 2L;

        var firstExpectingMeeting = new Meeting().setId(firstMeetingId);
        var secondExpectingMeeting = new Meeting().setId(secondMeetingId);

        when(meetingRepository.findAllByMemberMeetingOnPeriod(userId, from, to))
                .thenReturn(List.of(firstExpectingMeeting, secondExpectingMeeting));

        var actualMeetings = meetingService.getMemberMeetingsByPeriod(userId, from, to);

        assertThat(actualMeetings)
                .hasSize(2)
                .matches(meetings -> meetings.get(0).getId() == firstMeetingId)
                .matches(meetings -> meetings.get(1).getId() == secondMeetingId);
    }

    @Test
    public void changeMemberStatusByMeetingIdUserIdAndStatus_statusShouldBeChanged() {
        var meetingId = 1L;
        var userId = 1L;
        var status = MemberStatus.REJECTED;

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(
                new Meeting()
                        .setId(meetingId)
                        .setMembers(List.of(
                                new MeetingMember()
                                        .setId(new MemberMeetingId(userId, meetingId))
                                        .setMemberStatus(MemberStatus.ACCEPTED)
                        ))
        ));
        //when save meeting it returns this meeting
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(p -> p.getArguments()[0]);

        assertThat(meetingService.changeMemberStatus(meetingId, userId, status))
                .matches(meeting -> meeting.getId() == meetingId)
                .extracting(meeting -> meeting.getMembers().get(0))
                .matches(member -> member.getId().getUserId() == userId)
                .matches(member -> member.getMemberStatus() == status);
    }

    @Test
    public void getMeetingInfo_shouldReturnMeeting() {
        //expected response data
        var meetingId = 1L;
        var expectedCreator = new User()
                .setId(1L)
                .setFirstName("first")
                .setLastName("last");

        var expectedMeeting = new Meeting()
                .setTitle("title")
                .setId(1L)
                .setCreator(expectedCreator)
                .setStartTime(ZonedDateTime.now().plusHours(1))
                .setEndTime(ZonedDateTime.now().plusHours(2));

        expectedMeeting.setMembers(List.of(new MeetingMember()
                .setUser(expectedCreator)
                .setMemberStatus(MemberStatus.NONE)
                .setMeeting(expectedMeeting)
        ));

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(expectedMeeting));

        //check
        var resultMeeting = meetingService.getMeeting(meetingId);

        assertThat(resultMeeting)
                .matches(meeting -> meeting.getId().equals(expectedMeeting.getId()))
                .matches(meeting -> meeting.getTitle().equals(expectedMeeting.getTitle()))
                .matches(meeting -> meeting.getStartTime().isEqual(expectedMeeting.getStartTime()))
                .matches(meeting -> meeting.getEndTime().isEqual(expectedMeeting.getEndTime()));

        assertThat(resultMeeting.getCreator())
                .matches(creator -> creator.getId().equals(expectedCreator.getId()))
                .matches(creator -> creator.getFirstName().equals(expectedCreator.getFirstName()))
                .matches(creator -> creator.getLastName().equals(expectedCreator.getLastName()));

        assertThat(resultMeeting.getMembers().get(0))
                .matches(meetingMember -> meetingMember.getUser().getId().equals(expectedCreator.getId()))
                .matches(meetingMember -> meetingMember.getMeeting().getId().equals(expectedMeeting.getId()));
    }

    @Test
    public void tryToGetMeetingInfoWithNotExistingMeetingId_shouldThrowIllegalArgumentException() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> meetingService.getMeeting(1L));
    }

    @Test
    public void createMeeting_meetingRepositoryShouldBeCalledWithExpectedParamAndExpectedResultShouldBeReturned() {
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
        when(meetingRepository.save(argThat(meeting -> meeting.getCreator().equals(expectedCreator))))
                .thenReturn(expectedCreationResult);

        var result = meetingService.createMeeting(meetingCreate);
        assertThat(result)
                .matches(meeting -> meeting.getTitle().equals(expectedCreationResult.getTitle()))
                .matches(meeting -> meeting.getId().equals(expectedCreationResult.getId()))
                .matches(meeting -> meeting.getStartTime().isEqual(expectedCreationResult.getStartTime()))
                .matches(meeting -> meeting.getEndTime().isEqual(expectedCreationResult.getEndTime()))
                .matches(meeting -> meeting.getCreator().equals(expectedCreationResult.getCreator()))
                .matches(meeting -> meeting.getMembers().equals(expectedCreationResult.getMembers()));

        verify(notificationCreatorService, times(1)).createMeetingMembersNotifications(result.getId(), result.getStartTime());
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
