//package ru.shokhinsergey.consumer.Integrationtests;
//
//import com.icegreen.greenmail.configuration.GreenMailConfiguration;
//import com.icegreen.greenmail.util.GreenMail;
//import com.icegreen.greenmail.util.ServerSetupTest;
//import jakarta.mail.Folder;
//import jakarta.mail.MessagingException;
//import org.junit.jupiter.api.*;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import ru.shokhinsergey.consumer.service.MessageService;
//import ru.shokhinsergey.message.Message;
//
//import java.io.IOException;
//
//@Disabled
//@SpringBootTest
//public class MessageServiceWithMailServiceIntegrationTest {
//
//    private static final String USERNAME_AND_PASSWORD = "test";
//    private static final String EMAIL = "example@gmail.com";
//    private static final String SUBJECT = "Letter from App!";
//    private static final String FROM = "kafkaconsumer@kafkaconsumer.com";
//    private static final String GREETING = "Hello! Your account was created successfully!";
//    private static final Message CREATE_MESSAGE = Message.instanceOfMessageOnCreate(EMAIL);
//
//    private static GreenMail greenMail;
//
////    @Autowired
//    private static MessageService service;
//
//
//    @BeforeAll
//    static void beforeAll(){
//        var mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("127.0.0.1");
//        mailSender.setPort(3025);
//
//        service = new MessageService(mailSender);
//
//        greenMail = new GreenMail(ServerSetupTest.SMTP).withConfiguration(
//                GreenMailConfiguration.aConfig()
////                        .withUser(USERNAME_AND_PASSWORD, USERNAME_AND_PASSWORD);
//                        .withDisabledAuthentication());
//
//    }
//
//    @Test
//    void SendEmailOnCreateUser_ok_methodSendEmail() throws MessagingException, IOException {
//
//        greenMail.start();
//
//        service.sendEmail (CREATE_MESSAGE);
//        greenMail.waitForIncomingEmail(1);
//        try (var store = greenMail.getSmtp().createStore()) {
//
////            store.connect (USERNAME_AND_PASSWORD, USERNAME_AND_PASSWORD);
//            store.connect ();
//            Folder inboxFolder = store.getFolder("INBOX");
//            inboxFolder.open(Folder.READ_ONLY);
//
//            var mail = inboxFolder.getMessages()[0];
//            Assertions.assertEquals(inboxFolder.getMessageCount(), 1);
//            Assertions.assertEquals(mail.getSubject(), SUBJECT);
//            Assertions.assertEquals(mail.getContent().toString(), GREETING);
//            Assertions.assertEquals(mail.getFrom()[0].toString(), FROM);
//            Assertions.assertEquals(mail.getRecipients(jakarta.mail.Message.RecipientType.TO)[0].toString(), EMAIL);
//
//        }
//    }
//
//    @AfterAll
//    static void afterAll(){
//        greenMail.stop();
//    }
//}
