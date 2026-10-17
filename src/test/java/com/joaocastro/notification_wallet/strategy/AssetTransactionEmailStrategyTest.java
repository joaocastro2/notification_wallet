package com.joaocastro.notification_wallet.strategy;

import com.joaocastro.notification_wallet.dto.TransactionCreatedDto;
import com.joaocastro.notification_wallet.dto.TransactionCreatedEvent;
import com.joaocastro.notification_wallet.dto.TransactionType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetTransactionEmailStrategyTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private AssetTransactionEmailStrategy assetTransactionEmailStrategy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(assetTransactionEmailStrategy, "fromEmail", "no-reply@wallet.com");
        ReflectionTestUtils.setField(assetTransactionEmailStrategy, "defaultRecipient", "notifications@wallet.com");
    }

    @Test
    @DisplayName("Deve processar template e enviar e-mail com sucesso para TransactionCreatedEvent")
    void shouldSendEmailSuccessfullyForTransactionCreatedEvent() {
        UUID transactionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transactionId,
                "PETR4",
                TransactionType.BUY,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(35.50),
                BigDecimal.valueOf(3550.00),
                now
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("transaction-created"), any(Context.class))).thenReturn("<html>Transacao</html>");

        assetTransactionEmailStrategy.sendEmail(event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("transaction-created"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("transactionId")).isEqualTo(transactionId);
        assertThat(capturedContext.getVariable("assetSymbol")).isEqualTo("PETR4");
        assertThat(capturedContext.getVariable("type")).isEqualTo(TransactionType.BUY);
        assertThat(capturedContext.getVariable("quantity")).isEqualTo(BigDecimal.valueOf(100));
        assertThat(capturedContext.getVariable("unitPrice")).isEqualTo(BigDecimal.valueOf(35.50));
        assertThat(capturedContext.getVariable("totalValue")).isEqualTo(BigDecimal.valueOf(3550.00));
        assertThat(capturedContext.getVariable("createdAt")).isEqualTo(now);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve processar template e enviar e-mail com sucesso para TransactionCreatedDto")
    void shouldSendEmailSuccessfullyForTransactionCreatedDto() {
        UUID transactionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TransactionCreatedDto dto = new TransactionCreatedDto(
                transactionId,
                "VALE3",
                TransactionType.SELL,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60.00),
                BigDecimal.valueOf(3000.00),
                now
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("transaction-created"), any(Context.class))).thenReturn("<html>Transacao</html>");

        assetTransactionEmailStrategy.sendEmail(dto);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("transaction-created"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("assetSymbol")).isEqualTo("VALE3");
        assertThat(capturedContext.getVariable("type")).isEqualTo(TransactionType.SELL);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve permitir envio com destinatário customizado")
    void shouldSendEmailWithCustomRecipient() {
        UUID transactionId = UUID.randomUUID();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transactionId,
                "ITUB4",
                TransactionType.BUY,
                BigDecimal.TEN,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(300),
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("transaction-created"), any(Context.class))).thenReturn("<html>Transacao</html>");

        assetTransactionEmailStrategy.sendEmail(event, "investidor@exemplo.com");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o envio de e-mail falhar")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "PETR4",
                TransactionType.BUY,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("transaction-created"), any(Context.class))).thenReturn("<html>Transacao</html>");
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> assetTransactionEmailStrategy.sendEmail(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar e-mail");
    }
}
