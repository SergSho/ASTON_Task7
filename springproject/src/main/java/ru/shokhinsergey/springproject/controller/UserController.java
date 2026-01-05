// http://localhost:8080/swagger-ui/index.html

package ru.shokhinsergey.springproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidArgumentException;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidIdException;
import ru.shokhinsergey.springproject.service.UserService;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/users")
@Tag(name = "UserService API", description = "CRUD операции. Создание и удаление \"USER\" происходит с отправкой " +
        "сообщения в брокер \"KAFKA\".")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Поиск \"user\" по \"id\"",
            description = "Возвращает данные пользователя с указанным \"id\" из базы данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя найдены",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(allOf = {UserDtoResult.class, EntityModel.class}))),
                    @ApiResponse(responseCode = "400", description = "Введенный \"id\" меньше \"1\"",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным \"id\" не найден",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserDtoResult>> get(
            @Parameter(required = true, description = "Идентификатор пользователя", example = "1",
                    schema = @Schema(implementation = Integer.class))
            @PathVariable
            Integer id
    ) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.get(id);
        UserDtoResult result = optionalResult.stream().findAny().orElseThrow();

        EntityModel<UserDtoResult> entityModel = EntityModel.of(result);
        entityModel.add(linkForUpdate(result), linkForCreate(), linkForDelete(result), linkForGet(result).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }

    @Operation(
            summary = "Удаление \"user\" по \"id\"",
            description = "Удаляет данные пользователя с указанным \"id\" из базы данных",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Данные пользователя удалены",
                            content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "400", description = "Введенный \"id\" меньше \"1\"",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным \"id\" не найден",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    //Ссылка в headers
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(required = true, description = "Идентификатор пользователя", example = "1",
                    schema = @Schema(implementation = Integer.class))
            @PathVariable
            Integer id) {
        if (id <= 0) throw new NotValidIdException();
        userService.delete(id).stream().findAny().orElseThrow();
        return ResponseEntity.noContent()
                .header("_link", linkForCreate().toString())
                .build();
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создание \"user\" по введенным данным",
            description = "Возвращает данные нового пользователя после его сохранения в базе данных",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Данные пользователя сохранены",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(allOf = {UserDtoResult.class, EntityModel.class}))),
                    @ApiResponse(responseCode = "400", description = "Входные данные не прошли валидацию",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "409", description = "Указанный \"email\" уже сохранен в базе данных",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    public ResponseEntity<EntityModel<UserDtoResult>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    description = "Идентификатор пользователя",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDtoCreateAndUpdate.class)))
            @RequestBody
            @Validated
            UserDtoCreateAndUpdate userCreateDto, BindingResult errors)  {
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }
        UserDtoResult result = userService.create(userCreateDto);
        EntityModel<UserDtoResult> entityModel = EntityModel.of(result);
        entityModel.add(linkForCreate().withSelfRel(), linkForUpdate(result), linkForDelete(result), linkForGet(result));

        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Operation(
            summary = "Обновление \"user\" по \"id\" и введенным данным",
            description = "Возвращает данные пользователя с указанным \"id\" после его обновления в базе данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя обновлены",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(allOf = {UserDtoResult.class, EntityModel.class}))),
                    @ApiResponse(responseCode = "400", description = "Входные данные не прошли валидацию",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным \"id\" не найден",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "409", description = "Указанный \"email\" уже сохранен в базе данных",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserDtoResult>> update(
            @Parameter(required = true, description = "Идентификатор пользователя", example = "1",
                    schema = @Schema(implementation = Integer.class))
            @PathVariable
            Integer id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    description = "Идентификатор пользователя",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDtoCreateAndUpdate.class)))
            @RequestBody
            @Validated
            UserDtoCreateAndUpdate userCreateDto, BindingResult errors) {
        if (id <= 0) throw new NotValidIdException();
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }
        Optional<UserDtoResult> optionalResult = userService.update(userCreateDto, id);
        UserDtoResult result = optionalResult.stream().findAny().orElseThrow();

        EntityModel<UserDtoResult> entityModel = EntityModel.of(result);
        entityModel.add(linkForUpdate(result).withSelfRel(), linkForCreate(), linkForDelete(result), linkForGet(result));

        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }




    private Link linkForGet(UserDtoResult result) {
        return WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).get(result.getId()))
                .withRel("getById")
                .withTitle("get_user");
    }

    private Link linkForDelete(UserDtoResult result) {
        return WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).delete(result.getId()))
                .withRel("deleteById")
                .withTitle("delete_user");
    }

    private Link linkForUpdate(UserDtoResult result) {
        return WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).update(result.getId(), null, null))
                .withRel("updateById")
                .withTitle("update_user");
    }

    private Link linkForCreate() {
        return WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).create(null, null))
                .withRel("create")
                .withTitle("create_user");
    }

    private String messageFromErrors(BindingResult errors) {
        String lineSeparator = System.lineSeparator();
        return errors.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(lineSeparator));
    }
}
