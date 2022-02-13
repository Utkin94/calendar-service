package ru.interview.app.calendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.interview.app.calendar.dto.MeetingDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingQueryResponse {
    private List<MeetingDto> meetings;
}
