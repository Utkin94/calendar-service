package ru.interview.app.calendar.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMemberDto {
    private UserDto user;
    private Boolean invitationAccepted;
}
