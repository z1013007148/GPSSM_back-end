package com.hgx.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author fish
 * @create 2021-11-24 18:56
 */
@PropertySource(value = {"classpath:application.properties"})
@Component
@Slf4j
public class SendEmail {
    @Value("${from}")
    private String from;
    @Value("${mail.from.mail.passone}")
    private String passone;
    @Value("${mail.from.mail.passtwo}")
    private String passtwo;

    public void sendMail(String to, String subject, String content, Path path, String uuid)
            throws MessagingException, UnsupportedEncodingException {
        log.info("begin to send email " + uuid + " to " + to);

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.163.com");
        properties.setProperty("mail.smtp.uesrname", from);
        properties.setProperty("mail.smtp.password", passone);
        properties.setProperty("mail.smtp.defaultEncoding", "UTF-8");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.ssl.enable", "true");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, passtwo);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();
        BodyPart html = new MimeBodyPart();
        html.setContent(content, "text/html;charset=utf-8");
        multipart.addBodyPart(html);

        if (path != null) {
            File file = path.toFile();

            String fileName = file.getName();
//            log.info("filename >>>" + fileName);

            BodyPart bodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(file);
            bodyPart.setDataHandler(new DataHandler(source));

            bodyPart.setFileName(MimeUtility.encodeWord(fileName));
            multipart.addBodyPart(bodyPart);
        }

        message.setContent(multipart);
        Transport.send(message);
        log.info("send is over ^_^");
    }
}
