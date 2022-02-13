package ru.interview.app.calendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableTimeResponse {
    private ZonedDateTime startFreeInterval;
    private ZonedDateTime endFreeInterval;
}
