package ru.interview.app.calendar.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
public abstract class Aggregate implements Serializable {

    @Version
    private Long version = 0L;
}
