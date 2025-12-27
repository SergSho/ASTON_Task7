package ru.shokhinsergey.springproject.mapper;

import org.springframework.stereotype.Component;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.model.User;

@Component
public class UserDtoResultMapper implements Mapper<User, UserDtoResult> {
    @Override
    public UserDtoResult mapFrom(User user) {

        return UserDtoResult.Builder.builder()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setAge(user.getAge())
                .setCreated_At(user.getCreated_At())
                .build();
    }
}
