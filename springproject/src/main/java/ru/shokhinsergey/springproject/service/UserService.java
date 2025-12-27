package ru.shokhinsergey.springproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.shokhinsergey.message.Message;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.mapper.UserDtoResultMapper;
import ru.shokhinsergey.springproject.mapper.UserMapper;
import ru.shokhinsergey.springproject.model.User;
import ru.shokhinsergey.springproject.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;



@Service
@Transactional
public class UserService {

    @Value("${springproject.kafka.topic}")
    private String topic;

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final KafkaTemplate<Integer, Message> kafkaTemplate;

    private final UserMapper userMapper;
    private final UserDtoResultMapper userDtoMapper;
    private final UserRepository userRepository;

    @Autowired
    public UserService(KafkaTemplate<Integer,Message> kafkaTemplate, UserMapper userMapper, UserDtoResultMapper userDtoMapper, UserRepository userRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.userMapper = userMapper;
        this.userDtoMapper = userDtoMapper;
        this.userRepository = userRepository;
    }

    public Optional<UserDtoResult> get(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return Optional.empty();
        return userOptional.map(userDtoMapper::mapFrom).stream().findAny();
    }

    public Optional<UserDtoResult> delete(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
            User user = userOptional.get();

            //Async mode!!!
            CompletableFuture<SendResult<Integer, Message>> futureResult = kafkaTemplate.send(topic, user.getId(),
                    Message.instanceOfMessageOnDelete(user.getEmail()));

            futureResult.whenComplete((sendResult, exception) -> {
                if (exception == null) LOG.info("Message was sent successfully. " + sendResult.getRecordMetadata());
                else LOG.error("Message didn't send. Exception: {}, message: {}.", exception.getClass(),
                        exception.getMessage());
            });
            return userOptional.map(userDtoMapper::mapFrom).stream().findAny();
        }
        LOG.warn("User with specified id = {} not found", id);
        return Optional.empty();
    }

    public Optional<UserDtoResult> deleteWithManualMessageSending(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return Optional.empty();
        userRepository.deleteById(id);
        return userOptional.map(userDtoMapper::mapFrom).stream().findAny();
    }


    public UserDtoResult create(UserDtoCreateAndUpdate userCreateDto) {
        User createUser = userMapper.mapFrom(userCreateDto);
        createUser = userRepository.save(createUser);

        //Sync mode!!!
        try {
            SendResult<Integer, Message> message = kafkaTemplate.send(topic, createUser.getId(),
                    Message.instanceOfMessageOnCreate(createUser.getEmail())).get();
            LOG.info("Message was sent successfully. " + message.getRecordMetadata());

        } catch (Exception e) {
            LOG.error("Message didn't send. Exception: {}, message: {}.", e.getClass(), e.getMessage());
        }

        return userDtoMapper.mapFrom(createUser);
    }

    public UserDtoResult createWithManualMessageSending(UserDtoCreateAndUpdate userCreateDto) {
        User createUser = userMapper.mapFrom(userCreateDto);
        createUser = userRepository.save(createUser);
        return userDtoMapper.mapFrom(createUser);
    }

    public Optional<UserDtoResult> update(UserDtoCreateAndUpdate userUpdateDto, Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            updateUser(userUpdateDto, user);
        } else return Optional.empty();
        return userOptional.map(userDtoMapper::mapFrom).stream().findAny();
    }

    private void updateUser(UserDtoCreateAndUpdate userUpdateDto, User user) {
        user.setName(userUpdateDto.getName());
        user.setEmail(userUpdateDto.getEmail());
        user.setAge(userUpdateDto.getAge());
    }

}
