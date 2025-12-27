package ru.shokhinsergey.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import ru.shokhinsergey.message.Message;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String packages;

//    @Value("${spring.kafka.consumer.properties.spring.json.value.default.type}")
//    private String defaultType;

//    @Value("${spring.kafka.consumer.key-deserializer}")
//    private String keySerializer;

//    @Value("${spring.kafka.consumer.value-deserializer}")
//    private String valueSerializer;

    @Bean
    ConsumerFactory<Integer, Message> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        JsonDeserializer<Message> deserializer = new JsonDeserializer<>(Message.class);
        deserializer.addTrustedPackages(packages);

        return new DefaultKafkaConsumerFactory<>(config, new IntegerDeserializer(), deserializer);
    }
}
