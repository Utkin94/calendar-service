package ru.interview.app.calendar.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Accessors(chain = true)
@Setter
@Getter
@Entity
@Table(schema = "calendar", name = "users")
@SequenceGenerator(name = "user_id_seq", schema = "calendar", sequenceName = "users_id_seq", allocationSize = 1)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @OneToMany(
            mappedBy = "creator",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Meeting> createdMeetings;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<MeetingMember> meetingsMember;
}
