package ru.interview.app.calendar.test;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.dto.MeetingDto;
import ru.interview.app.calendar.dto.MeetingMemberDto;
import ru.interview.app.calendar.dto.UserDto;
import ru.interview.app.calendar.dto.request.AvailableTimeQuery;
import ru.interview.app.calendar.dto.request.MeetingCreate;
import ru.interview.app.calendar.dto.response.AvailableTimeResponse;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MemberStatus;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.utils.TimeUtils;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.interview.app.calendar.test.utils.TestUtils.isDate;

public class MeetingInboundE2ETest extends CalendarCommonE2ETest {

    @BeforeEach
    @Transactional
    public void beforeEach() {
        userRepository.deleteAll();
        meetingRepository.deleteAll();

        userRepository.save(new User()
                .setId(1L)
                .setFirstName("First")
                .setLastName("First")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
        userRepository.save(new User()
                .setId(2L)
                .setFirstName("Second")
                .setLastName("Second")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
        userRepository.save(new User()
                .setId(3L)
                .setFirstName("Third")
                .setLastName("Third")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
    }

    @Test
    public void getAvailabilityPeriod_shouldReturnValidPeriod() throws Exception {
        var now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(5);

        var expectedStartTimeRef = new AtomicReference<ZonedDateTime>();
        var expectedEndTimeRef = new AtomicReference<ZonedDateTime>();

        executeWithTransaction(() -> initIntervalTestData(now, expectedStartTimeRef, expectedEndTimeRef));

        var query = new AvailableTimeQuery()
                .setMinFreeIntervalToFind(60)
                .setUserIds(userRepository.findAll().stream()
                        .map(User::getId)
                        .collect(Collectors.toList())
                );

        var mvcResult = mockMvc.perform(post("/meetings/availableInterval")
                        .content(objectMapper.writeValueAsString(query))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AvailableTimeResponse.class);

        assertThat(response)
                .matches(r -> r.getStartFreeInterval().truncatedTo(ChronoUnit.MINUTES).isEqual(expectedStartTimeRef.get().truncatedTo(ChronoUnit.MINUTES)))
                .matches(r -> r.getEndFreeInterval().truncatedTo(ChronoUnit.MINUTES).isEqual(expectedEndTimeRef.get().truncatedTo(ChronoUnit.MINUTES)));
    }

    @Test
    public void getUserMeetingsByUserIdAndPeriod_meetingsShouldBeReturned() throws Exception {
        var threeMeetingsWithOneDayInterval = createThreeMeetingsWithOneDayInterval();

        var userId = threeMeetingsWithOneDayInterval.get(0).getCreator().getId();

        var firstExpectedMeeting = threeMeetingsWithOneDayInterval.get(0);
        var secondExpectedMeeting = threeMeetingsWithOneDayInterval.get(1);

        var from = firstExpectedMeeting.getStartTime().minusMinutes(30).format(ISO_DATE_TIME);
        var to = secondExpectedMeeting.getEndTime().minusMinutes(30).format(ISO_DATE_TIME);

        mockMvc.perform(get("/meetings/member/{userId}", userId)
                        .queryParam("from", from)
                        .queryParam("to", to))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meetings.size()", is(2)))
                .andExpect(jsonPath("$.meetings[0].id").value(firstExpectedMeeting.getId()))
                .andExpect(jsonPath("$.meetings[1].id").value(secondExpectedMeeting.getId()));
    }

    @Test
    public void putMeetingMemberByMeetingIdUserIdAndAcceptingStatus_acceptingStatusShouldBeSetForMeetingMember() throws Exception {
        //init test data
        var meeting = executeWithTransaction(t -> {
            var creator = userRepository.findAll().get(0);
            return createMeeting(creator, List.of(creator));
        });

        var meetingId = meeting.getId();
        var userId = meeting.getMembers().get(0).getUser().getId();

        mockMvc.perform(put("/meetings/{meetingId}/member/{userId}/{status}", meetingId, userId, MemberStatus.ACCEPTED))
                .andExpect(status().isOk());

        executeWithTransaction(() -> {
            var updatedMeeting = meetingRepository.getById(meeting.getId());

            assertThat(updatedMeeting.getMembers().get(0))
                    .matches(meetingMember -> meetingMember.getMemberStatus() == MemberStatus.ACCEPTED);
        });
    }

    @Test
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void postMeetingWithFewMembers_meetingShouldBeCreated() throws Exception {
        var users = userRepository.findAll();
        var meetingCreator = users.get(0);
        var secondUser = users.get(1);
        var thirdUser = users.get(2);

        var startTime = TimeUtils.roundUpToQuarterHour(ZonedDateTime.now(Clock.systemUTC()).plusHours(5));
        var endTime = startTime.plusHours(1);

        var createRequest = new MeetingCreate()
                .setCreatorId(meetingCreator.getId())
                .setTitle("meeting title")
                .setMeetingStartTime(startTime)
                .setMeetingEndTime(endTime)
                .setMemberUserIds(Set.of(secondUser.getId(), thirdUser.getId()));

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("length($.keys())").value(6L))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(createRequest.getTitle()))
                .andExpect(jsonPath("$.startTime", isDate(createRequest.getMeetingStartTime())))
                .andExpect(jsonPath("$.endTime", isDate(createRequest.getMeetingEndTime())))
                .andExpect(jsonPath("$.creator.id").value(meetingCreator.getId()))
                .andExpect(jsonPath("$.creator.firstName", is(meetingCreator.getFirstName())))
                .andExpect(jsonPath("$.creator.lastName", is(meetingCreator.getLastName())))
                .andExpect(jsonPath("$.members[0]").value(mapUserToMember(secondUser, MemberStatus.NONE)))
                .andExpect(jsonPath("$.members[1]").value(mapUserToMember(thirdUser, MemberStatus.NONE)));

        var createdMeeting = meetingRepository.findAll().get(0);
        assertThat(createdMeeting)
                .matches(meeting -> meeting.getId() != null)
                .matches(meeting -> meeting.getTitle().equals(createRequest.getTitle()))
                .matches(meeting -> meeting.getCreator().equals(meetingCreator))
                .matches(meeting -> meeting.getStartTime().isEqual(createRequest.getMeetingStartTime()))
                .matches(meeting -> meeting.getEndTime().isEqual(createRequest.getMeetingEndTime()));

        assertThat(createdMeeting.getMembers().get(0))
                .matches(member -> member.getUser().equals(secondUser))
                .matches(member -> member.getMeeting().getId().equals(createdMeeting.getId()));
    }

    @Test
    public void getMeetingInfo_shouldReturnMeetingInfo() throws Exception {
        //init test data
        var meeting = executeWithTransaction(t -> {
            var creator = userRepository.findAll().get(0);
            return createMeeting(creator, List.of(creator));
        });
        var creator = meeting.getCreator();

        //expected response
        var creatorDto = new UserDto(creator.getId(), creator.getFirstName(), creator.getLastName());
        var expectedMeetingDto = new MeetingDto(
                meeting.getId(),
                creatorDto,
                meeting.getTitle(),
                meeting.getStartTime(),
                meeting.getEndTime(),
                List.of(new MeetingMemberDto(creatorDto, MemberStatus.NONE))
        );

        //check
        mockMvc.perform(get("/meetings/{meetingId}", meeting.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedMeetingDto)));
    }

    @ParameterizedTest
    @MethodSource("meetingCreateBadRequests")
    public void tryToCreateMeetingWithIncorrectRequest_badRequestStatusCodeShouldBeReturned(MeetingCreate meetingCreate) throws Exception {
        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meetingCreate)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static List<MeetingCreate> meetingCreateBadRequests() {
        var correctStartTime = TimeUtils.roundUpToQuarterHour(ZonedDateTime.now().plusHours(1));
        var correctEndTime = correctStartTime.plusHours(1);

        return List.of(
                //null title
                new MeetingCreate()
                        .setCreatorId(1L)
                        .setMeetingStartTime(correctStartTime)
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(2L)),

                //blank title
                new MeetingCreate()
                        .setTitle("")
                        .setCreatorId(1L)
                        .setMeetingStartTime(correctStartTime)
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(2L)),

                //null not existing
                new MeetingCreate()
                        .setTitle("title")
                        .setMeetingStartTime(correctStartTime)
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(2L)),

                //not existed creator
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(999L)
                        .setMeetingStartTime(correctStartTime)
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(2L)),

                //startTime before now
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().minusHours(1))
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(2L)),

                //endTime before startTime
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(correctEndTime)
                        .setMeetingEndTime(correctStartTime)
                        .setMemberUserIds(Set.of(2L)),

                //startTime null
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingEndTime(correctStartTime)
                        .setMemberUserIds(Set.of(2L)),

                //endTime null
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(correctStartTime)
                        .setMemberUserIds(Set.of(2L)),

                //not existing member
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(correctStartTime)
                        .setMeetingEndTime(correctEndTime)
                        .setMemberUserIds(Set.of(999L)),

                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.parse("2007-12-03T10:35:00+01:00[Europe/Paris]"))
                        .setMeetingEndTime(ZonedDateTime.parse("2007-12-03T11:00:00+01:00[Europe/Paris]"))
                        .setMemberUserIds(Set.of(999L))
        );
    }

    private MeetingMemberDto mapUserToMember(User user, MemberStatus status) {
        return new MeetingMemberDto(
                new UserDto(user.getId(), user.getFirstName(), user.getFirstName()),
                status
        );
    }

    private List<Meeting> createThreeMeetingsWithOneDayInterval() {
        var firstMeetingStartDate = ZonedDateTime.of(
                2022, 6, 20, 14, 0, 0, 0, ZoneId.systemDefault());
        var firstMeetingEndDate = firstMeetingStartDate.plusHours(1);

        var secondMeetingStartDate = firstMeetingEndDate.plusDays(1);
        var secondMeetingEndDate = secondMeetingStartDate.plusHours(1);

        var thirdMeetingStartDate = secondMeetingEndDate.plusDays(1);
        var thirdMeetingEndDate = thirdMeetingStartDate.plusHours(1);

        return executeWithTransaction(t -> {
            var creator = userRepository.findAll().get(0);

            return List.of(
                    createMeeting(creator, List.of(creator), m -> m.setStartTime(firstMeetingStartDate).setEndTime(firstMeetingEndDate)),
                    createMeeting(creator, List.of(creator), m -> m.setStartTime(secondMeetingStartDate).setEndTime(secondMeetingEndDate)),
                    createMeeting(creator, List.of(creator), m -> m.setStartTime(thirdMeetingStartDate).setEndTime(thirdMeetingEndDate))
            );
        });
    }

    @NotNull
    private Meeting createMeeting(User creator, List<User> memberList) {
        return createMeeting(creator, memberList, null);
    }

    private void initIntervalTestData(ZonedDateTime now, AtomicReference<ZonedDateTime> expectedStartTimeRef, AtomicReference<ZonedDateTime> expectedEndTimeRef) {
        var users = userRepository.findAll();

        //this meeting makes it impossible to create meeting at least until start + 90 minutes
        createMeeting(users.get(0), List.of(users.get(0)), meeting -> {
            meeting.setStartTime(now.plusMinutes(30));
            meeting.setEndTime(now.plusMinutes(90));
            meeting.getMembers().get(0).setMemberStatus(MemberStatus.ACCEPTED);
        });

        //this meeting makes it impossible to create meeting at least until start + 120 minutes
        var secondMeeting = createMeeting(users.get(1), List.of(users.get(1)), meeting -> {
            meeting.setStartTime(now.plusMinutes(60));
            meeting.setEndTime(now.plusMinutes(120));
            meeting.getMembers().get(0).setMemberStatus(MemberStatus.ACCEPTED);
        });

        expectedStartTimeRef.set(secondMeeting.getEndTime());

        //this meeting DOES NOT make it impossible to create meeting, because it's member status is NONE
        createMeeting(users.get(1), List.of(users.get(1)), meeting -> {
            meeting.setStartTime(now);
            meeting.setEndTime(now.plusMinutes(3000));
            meeting.getMembers().get(0).setMemberStatus(MemberStatus.NONE);
        });

        //this meeting starts in 60 minutes from the end of the second meeting, so it's possible to have an hour meeting between them
        var lastMeeting = createMeeting(users.get(2), List.of(users.get(2)), meeting -> {
            meeting.setStartTime(now.plusMinutes(180));
            meeting.setEndTime(now.plusMinutes(200));
            meeting.getMembers().get(0).setMemberStatus(MemberStatus.ACCEPTED);
        });
        expectedEndTimeRef.set(lastMeeting.getStartTime());
    }
}
