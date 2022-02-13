package ru.interview.app.calendar.mapper;


import org.mapstruct.Mapper;
import ru.interview.app.calendar.controller.dto.MeetingDto;
import ru.interview.app.calendar.controller.dto.MeetingMemberDto;
import ru.interview.app.calendar.controller.dto.UserDto;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.User;

@Mapper(componentModel = "spring")
public interface CalendarMapper {

    User mapUserDtoToEntity(UserDto userDto);

    UserDto mapUserEntityToDto(User createdUser);

    MeetingDto mapMeetingEntityToDto(Meeting meeting);

    MeetingMemberDto mapMeetingMemberToDto(MeetingMember meetingMember);
}
