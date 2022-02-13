package ru.interview.app.calendar.controller.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MeetingCreate {

    @NotNull
    private Long creatorId;

    @NotBlank
    private String title;

    @Future
    @NotNull
    private ZonedDateTime meetingStartTime;

    @Future
    @NotNull
    private ZonedDateTime meetingEndTime;

    private Set<Long> memberUserIds;
}
