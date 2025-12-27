package ru.shokhinsergey.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.shokhinsergey.message.Message;

@Service
public class MessageService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Value("${consumer.mail.message.greeting}")
    private String greeting;

    @Value("${consumer.mail.message.farewell}")
    private String farewell;

    @Value("${consumer.mail.subject}")
    private String subject;

    @Value("${consumer.mail.from}")
    private String from;

    private final JavaMailSender mailSender;

    @Autowired
    public MessageService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private SimpleMailMessage prepareMail (String body, String email){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setSubject(subject);
        mail.setFrom(from);
        mail.setTo(email);
        mail.setText(body);
        return mail;
    }

    public void sendEmail(Message message){
        if (message.getOperation().equalsIgnoreCase("create"))
            mailSender.send(prepareMail(greeting, message.getEmail()));
        else mailSender.send(prepareMail(farewell, message.getEmail()));
        LOG.info("Email отправлен. Операция: {}, email: {}", message.getOperation(), message.getEmail());
    }
}
