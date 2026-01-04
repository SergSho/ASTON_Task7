package ru.shokhinsergey.consumer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import ru.shokhinsergey.consumer.service.MessageService;
import ru.shokhinsergey.message.Message;

@RestController

public class MessageController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final MessageService service;

    @Autowired
    public MessageController(MessageService service) {
        this.service = service;
    }

    @RequestMapping("/message")
    public void sendMail(@RequestBody Message message) {
        LOG.info("Message was received by Consumer (manual sending). Operation: {}, email: {}", message.getOperation(),
                message.getEmail());
        try {
            service.sendEmail(message);
        } catch (Exception e) {
            LOG.error("Message wasn't sent to specified email. Operation: {}, email: {}. Exception {}", message.getOperation(),
                    message.getEmail(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
