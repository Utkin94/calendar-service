package ru.interview.app.calendar.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.interview.app.calendar.entity.MeetingStatus;
import ru.interview.app.calendar.validation.HourQuarter;

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
    @HourQuarter
    private ZonedDateTime meetingStartTime;

    @Future
    @NotNull
    @HourQuarter
    private ZonedDateTime meetingEndTime;

    private MeetingStatus status = MeetingStatus.OPEN;

    private Set<Long> memberUserIds;
}
