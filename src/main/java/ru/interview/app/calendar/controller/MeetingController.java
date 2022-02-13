package ru.interview.app.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.interview.app.calendar.controller.dto.MeetingDto;
import ru.interview.app.calendar.controller.dto.request.MeetingCreate;
import ru.interview.app.calendar.mapper.CalendarMapper;
import ru.interview.app.calendar.service.MeetingService;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final CalendarMapper mapper;

    @PostMapping
    public MeetingDto createMeeting(@RequestBody MeetingCreate meetingCreate) {
        var createdMeeting = meetingService.createMeeting(meetingCreate);
        return mapper.mapMeetingEntityToDto(createdMeeting);
    }
}
