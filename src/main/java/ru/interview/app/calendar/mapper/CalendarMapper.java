package ru.interview.app.calendar.mapper;


import org.mapstruct.Mapper;
import ru.interview.app.calendar.dto.MeetingDto;
import ru.interview.app.calendar.dto.MeetingMemberDto;
import ru.interview.app.calendar.dto.UserDto;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CalendarMapper {

    User mapUserDtoToEntity(UserDto userDto);

    UserDto mapUserEntityToDto(User createdUser);

    MeetingDto mapMeetingEntityToDto(Meeting meeting);

    List<MeetingDto> mapMeetingEntitiesToDtoList(List<Meeting> meetings);

    MeetingMemberDto mapMeetingMemberToDto(MeetingMember meetingMember);
}
