package com.joaocastro.notification_wallet.strategy;

import com.joaocastro.notification_wallet.dto.UserCreatedDto;
import com.joaocastro.notification_wallet.dto.UserCreatedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEmailStrategy implements EmailStrategy<UserCreatedEvent> {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Override
    public void sendEmail(UserCreatedEvent event) {
        log.info("Processando envio de e-mail de boas-vindas para o usuário: userId={}, email={}", event.userId(), event.userMail());
        send(event.userName(), event.userMail());
    }

    public void sendEmail(UserCreatedDto dto) {
        log.info("Processando envio de e-mail de boas-vindas para o usuário (DTO): userId={}, email={}", dto.userId(), dto.userMail());
        send(dto.userName(), dto.userMail());
    }

    public void send(UserCreatedEvent event) {
        sendEmail(event);
    }

    public void send(UserCreatedDto dto) {
        sendEmail(dto);
    }

    private void send(String userName, String userMail) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("userMail", userMail);

            String htmlContent = templateEngine.process("user-created", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            String sender = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "no-reply@wallet.com";
            helper.setFrom(sender);
            helper.setTo(userMail);
            helper.setSubject("Bem-vindo ao Wallet");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("E-mail de boas-vindas enviado com sucesso para {}", userMail);
        } catch (MessagingException | MailException e) {
            log.error("Erro ao enviar e-mail de boas-vindas para {}: {}", userMail, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail para: " + userMail, e);
        }
    }
}
