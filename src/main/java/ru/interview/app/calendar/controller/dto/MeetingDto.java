package ru.interview.app.calendar.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private UserDto creator;
    private String title;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private List<MeetingMemberDto> members;
}
