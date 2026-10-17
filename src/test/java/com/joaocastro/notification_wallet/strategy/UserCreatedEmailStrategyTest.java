package com.joaocastro.notification_wallet.strategy;

import com.joaocastro.notification_wallet.dto.UserCreatedDto;
import com.joaocastro.notification_wallet.dto.UserCreatedEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreatedEmailStrategyTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private UserCreatedEmailStrategy userCreatedEmailStrategy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userCreatedEmailStrategy, "fromEmail", "no-reply@wallet.com");
    }

    @Test
    @DisplayName("Deve processar template e enviar e-mail com sucesso para UserCreatedEvent")
    void shouldSendEmailSuccessfullyForUserCreatedEvent() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "Alice", "alice@example.com", LocalDateTime.now());

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("user-created"), any(Context.class))).thenReturn("<html>Bem-vindo Alice</html>");

        userCreatedEmailStrategy.sendEmail(event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("user-created"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("userName")).isEqualTo("Alice");
        assertThat(capturedContext.getVariable("userMail")).isEqualTo("alice@example.com");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve processar template e enviar e-mail com sucesso para UserCreatedDto")
    void shouldSendEmailSuccessfullyForUserCreatedDto() {
        UUID userId = UUID.randomUUID();
        UserCreatedDto dto = new UserCreatedDto(userId, "Bob", "bob@example.com", LocalDateTime.now());

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("user-created"), any(Context.class))).thenReturn("<html>Bem-vindo Bob</html>");

        userCreatedEmailStrategy.sendEmail(dto);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("user-created"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("userName")).isEqualTo("Bob");
        assertThat(capturedContext.getVariable("userMail")).isEqualTo("bob@example.com");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o envio de e-mail falhar")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() {
        UserCreatedEvent event = new UserCreatedEvent(UUID.randomUUID(), "Alice", "alice@example.com", LocalDateTime.now());

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("user-created"), any(Context.class))).thenReturn("<html>Bem-vindo Alice</html>");
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> userCreatedEmailStrategy.sendEmail(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar e-mail");
    }
}
