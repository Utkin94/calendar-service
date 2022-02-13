package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.UserService;
import ru.interview.app.calendar.service.ValidationService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ValidationService validationService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User createUser(User userToCreate) {
        validationService.validate(userToCreate);
        return userRepository.save(userToCreate);
    }
}
