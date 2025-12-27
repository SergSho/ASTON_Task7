package ru.shokhinsergey.consumer.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.shokhinsergey.consumer.service.MessageService;
import ru.shokhinsergey.message.Message;

@Component
@KafkaListener(topics = "user-event", groupId = "user-event")
public class MessageHandler {
    private MessageService service;

    @Autowired
    public MessageHandler(MessageService service) {
        this.service = service;
    }

    @KafkaHandler
    public void sendMessage (@RequestBody Message message) {
        service.sendEmail(message);
    }
}
