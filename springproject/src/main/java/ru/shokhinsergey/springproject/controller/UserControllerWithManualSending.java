package ru.shokhinsergey.springproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
//import org.springframework.cloud.openfeign.FeignClient;

import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidArgumentException;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidIdException;
import ru.shokhinsergey.springproject.service.UserService;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerWithManualSending {
    private final UserService userService;

    @Autowired
    public UserControllerWithManualSending(UserService userService) {
        this.userService = userService;
    }

    // FOR MANUAL SENDING
    @DeleteMapping("/{id}")
    public UserDtoResult deleteWithManualMessageSending(@PathVariable Integer id) {
        if (id <= 0) throw new NotValidIdException();
        Optional<UserDtoResult> optionalResult = userService.deleteWithManualMessageSending(id);
        return optionalResult.stream().findAny().orElseThrow();
    }

    // FOR MANUAL SENDING
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
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
}
