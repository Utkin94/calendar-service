package ru.interview.app.calendar.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import ru.interview.app.calendar.dto.UserDto;
import ru.interview.app.calendar.entity.User;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserInboundE2ETest extends CalendarCommonE2ETest {

    @BeforeEach
    @Transactional
    public void beforeEach() {
        userRepository.deleteAll();

        userRepository.save(new User()
                .setId(1L)
                .setFirstName("First")
                .setLastName("First")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
        userRepository.save(new User()
                .setId(2L)
                .setFirstName("Second")
                .setLastName("Second")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
        userRepository.save(new User()
                .setId(3L)
                .setFirstName("Third")
                .setLastName("Third")
                .setMeetingsMember(new ArrayList<>())
                .setCreatedMeetings(new ArrayList<>())
        );
    }

    @Test
    @Transactional
    public void postNewUser_userShouldBePersistedToDatabase() throws Exception {
        var userId = 4L;
        var firstName = "first";
        var secondName = "second";

        var userDtoToCreate = new UserDto(userId, firstName, secondName);
        var createdUser = new User()
                .setId(userId)
                .setFirstName(firstName)
                .setLastName(secondName);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDtoToCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(userDtoToCreate)));

        assertThat(userRepository.getById(userId))
                .matches(user -> user.getId().equals(createdUser.getId()))
                .matches(user -> user.getFirstName().equals(createdUser.getFirstName()))
                .matches(user -> user.getLastName().equals(createdUser.getLastName()));
    }

    @ParameterizedTest
    @MethodSource("invalidUsersDto")
    public void postUsersWithInvalidRequest_shouldReturnCreatedUserWithStatus200(UserDto invalidUser) throws Exception {
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static List<UserDto> invalidUsersDto() {
        return List.of(
                new UserDto(null, null, "lastName"),
                new UserDto(null, "firstName", null)
        );
    }

}
