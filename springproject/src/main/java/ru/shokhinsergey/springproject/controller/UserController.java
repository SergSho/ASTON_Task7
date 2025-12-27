package ru.shokhinsergey.springproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import ru.shokhinsergey.message.Message;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidArgumentException;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidIdException;
import ru.shokhinsergey.springproject.service.UserService;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final String URL = "http://localhost:8181/message";

    private final UserService userService;
    private final RestTemplate restTemplate;

    @Autowired
    public UserController(UserService userService, RestTemplate restTemplate) {

        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/{id}")
    public UserDtoResult get(@PathVariable Integer id) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.get(id);
        return optionalResult.stream().findAny().orElseThrow();
    }

    @DeleteMapping("/{id}")
    public UserDtoResult delete(@PathVariable Integer id) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.delete(id);
        return optionalResult.stream().findAny().orElseThrow();
    }

    @DeleteMapping("/message/{id}")
    public UserDtoResult deleteWithManualMessageSending(@PathVariable Integer id) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.deleteWithManualMessageSending(id);
        UserDtoResult result = optionalResult.stream().findAny().orElseThrow();

        Message message = Message.instanceOfMessageOnDelete(result.getEmail());
        ResponseEntity<Void> response = restTemplate.postForEntity(URL, message, Void.class);
        if (!response.getStatusCode().is2xxSuccessful())
            throw new RuntimeException("User deleted from DB. Message not sent by email");
        return result;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserDtoResult create(@RequestBody @Validated UserDtoCreateAndUpdate userCreateDto,
                                BindingResult errors) {
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }

        return userService.create(userCreateDto);
    }

    @PostMapping("/message")
    public UserDtoResult createWithManualMessageSending(@RequestBody @Validated UserDtoCreateAndUpdate userCreateDto,
                                                        BindingResult errors) {
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }
        UserDtoResult result = userService.createWithManualMessageSending(userCreateDto);
        Message message = Message.instanceOfMessageOnCreate(result.getEmail());
        ResponseEntity<Void> response = restTemplate.postForEntity(URL, message, Void.class);
        if (!response.getStatusCode().is2xxSuccessful())
            throw new RuntimeException("User saved in DB. Message not sent by email.");
        return result;
    }

    private String messageFromErrors(BindingResult errors) {
        String lineSeparator = System.lineSeparator();
        return errors.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(lineSeparator));
    }

    @PutMapping("/{id}")
    public UserDtoResult update(@PathVariable Integer id,
                                @RequestBody @Validated UserDtoCreateAndUpdate userCreateDto,
                                BindingResult errors) {
        if (id <= 0) throw new NotValidIdException();
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }
        Optional<UserDtoResult> optionalResult = userService.update(userCreateDto, id);
        return optionalResult.stream().findAny().orElseThrow();
    }
}
