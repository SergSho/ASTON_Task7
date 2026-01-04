package ru.shokhinsergey.springproject.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.openfeign.FeignClient;
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

// TO DO - посмотреть возможность использования @FeignClient взамен RestTemplate
//@FeignClient(name = "consumer")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    // FOR MANUAL SENDING
    @DeleteMapping("/message/{id}")
    public UserDtoResult deleteWithManualMessageSending(@PathVariable Integer id) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.deleteWithManualMessageSending(id);
        return optionalResult.stream().findAny().orElseThrow();
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

    // FOR MANUAL SENDING
    @PostMapping("/message")
    public UserDtoResult createWithManualMessageSending(@RequestBody @Validated UserDtoCreateAndUpdate userCreateDto,
                                                        BindingResult errors) {
        if (errors.hasErrors()) {
            String message = messageFromErrors(errors);
            throw new NotValidArgumentException(message);
        }
        return userService.createWithManualMessageSending(userCreateDto);
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
