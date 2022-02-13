package ru.interview.app.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.interview.app.calendar.controller.advice.ApiError;
import ru.interview.app.calendar.dto.MeetingDto;
import ru.interview.app.calendar.dto.request.AvailableTimeQuery;
import ru.interview.app.calendar.dto.request.MeetingCreate;
import ru.interview.app.calendar.dto.response.AvailableTimeResponse;
import ru.interview.app.calendar.dto.response.MeetingQueryResponse;
import ru.interview.app.calendar.entity.MemberStatus;
import ru.interview.app.calendar.mapper.CalendarMapper;
import ru.interview.app.calendar.service.MeetingService;

import java.time.ZonedDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final CalendarMapper mapper;

    @Operation(summary = "Создание новой втречи")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Встреча успешно создана",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MeetingDto.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))})
    })
    @PostMapping
    public MeetingDto createMeeting(@RequestBody MeetingCreate meetingCreate) {

        var createdMeeting = meetingService.createMeeting(meetingCreate);
        return mapper.mapMeetingEntityToDto(createdMeeting);
    }

    @Operation(summary = "Получение встречи по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Встреча успешно создана",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MeetingDto.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))})
    })
    @GetMapping("/{meetingId}")
    public MeetingDto getMeeting(@PathVariable @Parameter(description = "Идентификтор встречи") Long meetingId) {

        var meeting = meetingService.getMeeting(meetingId);
        return mapper.mapMeetingEntityToDto(meeting);
    }

    @Operation(summary = "Изменение статуса участника встречи")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно изменен",
                    content = {@Content}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content})
    })
    @PutMapping("/{meetingId}/member/{userId}/{status}")
    public void changeMeetingMemberStatus(@PathVariable @Parameter(description = "Идентификтор встречи") Long meetingId,
                                          @PathVariable @Parameter(description = "Идентификтор участника") Long userId,
                                          @PathVariable @Parameter(description = "Новый статус участника") MemberStatus status) {

        meetingService.changeMemberStatus(meetingId, userId, status);
    }

    @Operation(summary = "Получение всех открытых встреч пользователя по заданному периоду")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список встреч получен",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MeetingQueryResponse.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content})
    })
    @GetMapping("/member/{userId}")
    public MeetingQueryResponse getMemberMeetingsByPeriod(
            @PathVariable @Parameter(description = "идентификтор пользователя") Long userId,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) @Parameter(description = "Начало периода") ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) @Parameter(description = "Конец периода") ZonedDateTime to) {

        var meetings = meetingService.getMemberMeetingsByPeriod(userId, from, to);
        return new MeetingQueryResponse(mapper.mapMeetingEntitiesToDtoList(meetings));
    }

    @Operation(summary = "Получение первого достаточного интервала времени в котором все пользователи свободны")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Интервал получен",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AvailableTimeResponse.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content})
    })
    @PostMapping("/availableInterval")
    public AvailableTimeResponse getAvailableTime(@RequestBody AvailableTimeQuery query) {

        return meetingService.getAvailableTime(query);
    }
}
