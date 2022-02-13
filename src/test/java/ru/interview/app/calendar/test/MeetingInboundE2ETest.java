package ru.interview.app.calendar.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.controller.dto.MeetingDto;
import ru.interview.app.calendar.controller.dto.MeetingMemberDto;
import ru.interview.app.calendar.controller.dto.UserDto;
import ru.interview.app.calendar.controller.dto.request.MeetingCreate;
import ru.interview.app.calendar.entity.User;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeetingInboundE2ETest extends CalendarCommonE2ETest {

    @BeforeEach
    @Transactional
    public void beforeEach() {
        userRepository.deleteAll();

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
    @Transactional
    public void postMeetingWithFewMembers_meetingShouldBeCreated() throws Exception {
        var users = userRepository.findAll();
        var meetingCreator = users.get(0);
        var secondUser = users.get(1);
        var thirdUser = users.get(2);

        var meetingTitle = "meeting title";
        var startTime = ZonedDateTime.now(Clock.systemUTC()).plusHours(5);
        var endTime = startTime.plusHours(1);

        var createRequest = new MeetingCreate()
                .setCreatorId(meetingCreator.getId())
                .setTitle(meetingTitle)
                .setMeetingStartTime(startTime)
                .setMeetingEndTime(endTime)
                .setMemberUserIds(Set.of(secondUser.getId(), thirdUser.getId()));

        var expectedResponse = new MeetingDto(
                1L,
                new UserDto(meetingCreator.getId(), meetingCreator.getFirstName(), meetingCreator.getLastName()),
                meetingTitle,
                startTime,
                endTime,
                List.of(
                        mapUserToMember(secondUser, false),
                        mapUserToMember(thirdUser, false)
                )
        );

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

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
        return List.of(
                //null title
                new MeetingCreate()
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(2L)),

                //blank title
                new MeetingCreate()
                        .setTitle("")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(2L)),

                //null not existing
                new MeetingCreate()
                        .setTitle("title")
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(2L)),

                //not existed creator
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(999L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(2L)),

                //startTime before now
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().minusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(2L)),

                //endTime before startTime
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(2))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(1))
                        .setMemberUserIds(Set.of(2L)),

                //startTime null
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(1))
                        .setMemberUserIds(Set.of(2L)),

                //endTime null
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMemberUserIds(Set.of(2L)),

                //not existing member
                new MeetingCreate()
                        .setTitle("title")
                        .setCreatorId(1L)
                        .setMeetingStartTime(ZonedDateTime.now().plusHours(1))
                        .setMeetingEndTime(ZonedDateTime.now().plusHours(2))
                        .setMemberUserIds(Set.of(999L))
        );
    }


    private MeetingMemberDto mapUserToMember(User user, boolean accepted) {
        return new MeetingMemberDto(
                new UserDto(user.getId(), user.getFirstName(), user.getFirstName()),
                accepted
        );
    }

}
