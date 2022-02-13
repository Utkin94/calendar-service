package ru.interview.app.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interval {
    private ZonedDateTime starTime;
    private ZonedDateTime endTime;
}
