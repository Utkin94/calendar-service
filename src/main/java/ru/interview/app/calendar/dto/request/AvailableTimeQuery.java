package ru.interview.app.calendar.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Accessors(chain = true)
public class AvailableTimeQuery {
    @NotNull
    @Positive
    private Integer minFreeIntervalToFind;
    @NotEmpty
    private List<Long> userIds;
}
