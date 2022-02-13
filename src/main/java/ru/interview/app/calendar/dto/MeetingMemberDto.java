package ru.interview.app.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.interview.app.calendar.entity.MemberStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMemberDto {
    private UserDto user;
    private MemberStatus memberStatus;
}
