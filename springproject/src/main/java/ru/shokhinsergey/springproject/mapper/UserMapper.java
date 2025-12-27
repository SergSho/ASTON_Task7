package ru.shokhinsergey.springproject.mapper;

import org.springframework.stereotype.Component;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.model.User;

@Component
public class UserMapper implements Mapper<UserDtoCreateAndUpdate, User>{
    @Override
    public User mapFrom(UserDtoCreateAndUpdate userDto) {
        return new User(userDto.getName(), userDto.getEmail(), userDto.getAge());
    }
}
