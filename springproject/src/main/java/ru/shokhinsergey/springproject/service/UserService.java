package ru.shokhinsergey.springproject.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;
import ru.shokhinsergey.message.Message;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.mapper.UserDtoResultMapper;
import ru.shokhinsergey.springproject.mapper.UserMapper;
import ru.shokhinsergey.springproject.model.User;
import ru.shokhinsergey.springproject.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;


// TO DO - посмотреть возможность использования @FeignClient взамен RestTemplate
//@FeignClient()
@Service
@Transactional
public class UserService {

    // FOR MANUAL SENDING
    private final RestTemplate restTemplate;
    //discovery-service (by app's name) - CHECKED
    private final String URL = "http://consumer/message";


    @Value("${springproject.kafka.topic}")
    private String topic;

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final KafkaTemplate<Integer, Message> kafkaTemplate;

    private final UserMapper userMapper;
    private final UserDtoResultMapper userDtoMapper;
    private final UserRepository userRepository;
    private final CircuitBreakerFactory<?, ?> cbf;

    @Autowired
    public UserService(KafkaTemplate<Integer, Message> kafkaTemplate, UserMapper userMapper,
                       UserDtoResultMapper userDtoMapper, UserRepository userRepository,
                       RestTemplate restTemplate, CircuitBreakerFactory<?, ?> cbf) {
        this.kafkaTemplate = kafkaTemplate;
        this.userMapper = userMapper;
        this.userDtoMapper = userDtoMapper;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.cbf = cbf;
    }

    public Optional<UserDtoResult> get(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return Optional.empty();
        return userOptional.map(userDtoMapper::mapFrom).stream().findAny();
    }


    // CircuitBreaker ("kafkaSendingBreaker") controls only sending message (method - "sendToKafka")
    public Optional<UserDtoResult> delete(Integer id) {
        Optional<User> userOptional = onDelete(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Message message = Message.instanceOfMessageOnDelete(user.getEmail());

            sendToKafka(user, message);

            LOG.info("PrimaryMethod. User was deleted successfully.");
            return userOptional.map(userDtoMapper::mapFrom).stream().findFirst();
        }
        LOG.warn("PrimaryMethod. User with specified id = {} is not found", id);
        return Optional.empty();
    }


    // CircuitBreaker ("kafkaSendingBreaker") controls only sending message (method - "sendToKafka")
    public UserDtoResult create(UserDtoCreateAndUpdate userCreateDto) {
        User createUser = userMapper.mapFrom(userCreateDto);
        createUser = userRepository.save(createUser);
        Message message = Message.instanceOfMessageOnCreate(createUser.getEmail());

        sendToKafka(createUser, message);

        LOG.info("PrimaryMethod. User was created successfully");
        return userDtoMapper.mapFrom(createUser);
    }

    // CircuitBreaker - "kafkaSendingBreaker"
    private void sendToKafka(User user, Message message) {
        org.springframework.cloud.client.circuitbreaker.CircuitBreaker breaker = cbf.create("kafkaSendingBreaker");
        breaker.run(() -> {
                SendResult<Integer, Message> mess;
                try {
                    mess = kafkaTemplate.send(topic, user.getId(), message).get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("PrimaryMethod. Exception when sending {}", e.getMessage());
                    throw new RuntimeException(e);
                }
                LOG.info("PrimaryMethod. Message was sent to Kafka successfully. " + mess.getRecordMetadata());
                return null;
            },
            throwable -> {
                LOG.error("FallbackMethod. Exception when sending {}", throwable.getMessage());
                return null;
            });
    }

    private Optional<User> onDelete(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
        }
        return userOptional;
    }

    // CircuitBreaker ("manualSendingBreaker") controls whole method (all exception!!!)
    @CircuitBreaker(name = "manualSendingBreaker", fallbackMethod = "defaultDeleteWithoutSending")
    public Optional<UserDtoResult> deleteWithManualMessageSending(Integer id) {
        Optional<User> userOptional = onDelete(id);
        if (userOptional.isPresent()) {
            Message message = Message.instanceOfMessageOnDelete(userOptional.get().getEmail());

            restTemplate.postForObject(URL, message, Void.class);

            LOG.info("PrimaryMethod. User was deleted successfully");
            LOG.info("PrimaryMethod. Message was sent to \"{}\" successfully", message.getEmail());
            return userOptional.map(userDtoMapper::mapFrom).stream().findFirst();
        }
        LOG.info("PrimaryMethod. User with specifies id = {} is not found", id);
        return Optional.empty();
    }

    // Delete without sending
    public Optional<UserDtoResult> defaultDeleteWithoutSending(Integer id, Throwable throwable) {
        Optional<User> userOptional = onDelete(id);
        LOG.info("FallbackMethod(without sending message). User was deleted successfully.");
        LOG.error("FallbackMethod(without sending message). Exception when sending {}", throwable.getMessage());
        return userOptional.map(userDtoMapper::mapFrom).stream().findFirst();
    }

    // CircuitBreaker ("manualSendingBreaker") controls whole method (all exception!!!)
    @CircuitBreaker(name = "manualSendingBreaker", fallbackMethod = "defaultCreateWithoutSending")
    public UserDtoResult createWithManualMessageSending(UserDtoCreateAndUpdate userCreateDto) {
        User createUser = userMapper.mapFrom(userCreateDto);
        createUser = userRepository.save(createUser);
        Message message = Message.instanceOfMessageOnCreate(createUser.getEmail());

        restTemplate.postForObject(URL, message, Void.class);

        LOG.info("PrimaryMethod. User was created successfully");
        LOG.info("PrimaryMethod. Message was sent to \"{}\" successfully", message.getEmail());
        return userDtoMapper.mapFrom(createUser);
    }

    // Create without sending
    public UserDtoResult defaultCreateWithoutSending(UserDtoCreateAndUpdate userCreateDto,
                                                     Throwable throwable) {

        User createUser = userMapper.mapFrom(userCreateDto);
        createUser = userRepository.save(createUser);
        LOG.info("FallbackMethod(without sending message). User was created successfully");
        LOG.error("FallbackMethod(without sending message). Exception when sending {}", throwable.getMessage());
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
