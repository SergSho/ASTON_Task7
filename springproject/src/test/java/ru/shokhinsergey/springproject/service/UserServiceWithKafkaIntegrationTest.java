package ru.shokhinsergey.springproject.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.shokhinsergey.message.Message;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.model.User;
import ru.shokhinsergey.springproject.repository.UserRepository;

import java.util.Map;
import java.util.Optional;


//Пересоздание тестового контекста в случае изменения его состояния в тестовом методе (изоляция тестов)
@DirtiesContext
//Повторное использование объекта тестового класса для всех входящих тестовых методов (BeforeAll - можно не static)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Перезаписать properties из основного конфига тестовыми
@ActiveProfiles("test")
@EmbeddedKafka(topics = "user-event", partitions = 1, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class UserServiceWithKafkaIntegrationTest {
    private final static String CREATE = "create";
    private final static String DELETE = "delete";
    private final static String NAME = "Example";
    private final static int AGE = 25;
    private  final static String EMAIL = "example@mail.ru";
    private  final static Integer ID = 1;

    @MockitoBean
    private UserRepository repository;

    @Autowired
    private Environment env;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private UserService service;

    private UserDtoCreateAndUpdate userDtoCreateAndUpdate;
    private Consumer<Integer, Message> consumer;
    private String topicName;
    private User userBeforeDB;
    private User userAfterDB;


    @BeforeAll
    public void init (){

        userDtoCreateAndUpdate = new UserDtoCreateAndUpdate(NAME, EMAIL, AGE);
        userBeforeDB = new User(NAME, EMAIL, AGE);
        userAfterDB = new User(ID, NAME, EMAIL, AGE);
        topicName = env.getProperty("test.topic-name");

        JsonDeserializer<Message> deserializer = new JsonDeserializer <>(Message.class);
        deserializer.addTrustedPackages(env.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        ConsumerFactory<Integer, Message> consumerFactory = new DefaultKafkaConsumerFactory<>(
                getConsumerProperties(),
                new IntegerDeserializer(),
                deserializer
        );
        consumer = consumerFactory.createConsumer();
        // Подписать потребителя на топик
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
    }

    private Map<String,Object> getConsumerProperties(){
        Map<String,Object> consumerProps = KafkaTestUtils.consumerProps(
                env.getProperty("spring.kafka.consumer.group-id"),
                env.getProperty("test.auto-commit"),
                embeddedKafkaBroker
                );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                env.getProperty ("spring.kafka.consumer.auto-offset-reset"));
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        return consumerProps;
    }

    @Test
    @DisplayName("Отправка сообщения в \"Kafka\" при создании нового \"User\".")
    void sendMessageToKafka_Ok_MethodCreate ()  {

        Mockito.doReturn(userAfterDB).when(repository).save(userBeforeDB);

        service.create (userDtoCreateAndUpdate);

        ConsumerRecord<Integer, Message> result = KafkaTestUtils.getSingleRecord(consumer, topicName);

        Message message = result.value();
        Integer key = result.key();

        Assertions.assertEquals(ID, key);
        Assertions.assertEquals(CREATE, message.getOperation());
        Assertions.assertEquals(EMAIL, message.getEmail());
    }



    @Test
    @DisplayName("Отправка сообщения в \"Kafka\" при удалении \"User\".")
    void sendMessageToKafka_Ok_MethodDelete () throws InterruptedException {

        Mockito.doReturn(Optional.of(userAfterDB)).when(repository).findById(ID);

        service.delete(ID);
        ConsumerRecord<Integer, Message> result = KafkaTestUtils.getSingleRecord(consumer, topicName);

        Message message = result.value();
        Integer key = result.key();

        Mockito.verify(repository, Mockito.times(1)).deleteById(ID);
        Assertions.assertEquals(ID, key);
        Assertions.assertEquals(DELETE, message.getOperation());
        Assertions.assertEquals(EMAIL, message.getEmail());
    }
}
