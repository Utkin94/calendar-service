package ru.interview.app.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MemberMeetingId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "meeting_id")
    private Long meetingId;
}
