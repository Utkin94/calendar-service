package ru.interview.app.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.interview.app.calendar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
