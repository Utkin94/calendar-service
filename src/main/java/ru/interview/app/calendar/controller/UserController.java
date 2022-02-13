package ru.interview.app.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.interview.app.calendar.controller.dto.UserDto;
import ru.interview.app.calendar.mapper.CalendarMapper;
import ru.interview.app.calendar.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/users", consumes = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final CalendarMapper userMapper;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDtoToCreate) {
        var userToCreate = userMapper.mapUserDtoToEntity(userDtoToCreate);
        var createdUser = userService.createUser(userToCreate);

        return userMapper.mapUserEntityToDto(createdUser);
    }
}
