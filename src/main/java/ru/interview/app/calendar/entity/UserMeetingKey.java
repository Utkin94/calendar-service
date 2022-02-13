package ru.interview.app.calendar.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Accessors(chain = true)
@Setter
@Getter
@Embeddable
public class UserMeetingKey implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "meeting_id")
    private Long meetingId;
}
