package ru.interview.app.calendar.test.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.UserRepository;
import ru.interview.app.calendar.service.impl.UserServiceImpl;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeAll
    public void init(@Mock UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userService = new UserServiceImpl(userRepository);
    }

    @BeforeEach
    public void afterEach() {
        Mockito.reset(userRepository);
    }

    @Test
    public void createUser_shouldCallRepositorySaveMethodWithValidUserEntity() {
        var firstName = "firstName";
        var lastName = "lastName";

        var userToCreate = new User()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setCreatedMeetings(new ArrayList<>())
                .setMeetingsMember(new ArrayList<>());

        var createdUser = new User()
                .setId(1L)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setCreatedMeetings(new ArrayList<>())
                .setMeetingsMember(new ArrayList<>());

        when(userRepository.save(userToCreate)).thenReturn(createdUser);

        assertThat(userService.createUser(userToCreate)).isEqualTo(createdUser);

        verify(userRepository, atMostOnce()).save(userToCreate);
    }

}
