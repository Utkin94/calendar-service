package ru.interview.app.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.interview.app.calendar.controller.advice.ApiError;
import ru.interview.app.calendar.dto.UserDto;
import ru.interview.app.calendar.mapper.CalendarMapper;
import ru.interview.app.calendar.service.UserService;

@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final CalendarMapper userMapper;

    @Operation(summary = "Создание нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно создан",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Указаны некорректные данные",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))})
    })
    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDtoToCreate) {
        var userToCreate = userMapper.mapUserDtoToEntity(userDtoToCreate);
        var createdUser = userService.createUser(userToCreate);

        return userMapper.mapUserEntityToDto(createdUser);
    }
}
