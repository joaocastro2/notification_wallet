package com.joaocastro.notification_wallet.strategy;

import com.joaocastro.notification_wallet.dto.TransactionCreatedDto;
import com.joaocastro.notification_wallet.dto.TransactionCreatedEvent;
import com.joaocastro.notification_wallet.dto.TransactionType;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetTransactionEmailStrategy implements EmailStrategy<TransactionCreatedEvent> {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Value("${notification.mail.default-recipient:notifications@wallet.com}")
    private String defaultRecipient;

    @Override
    public void sendEmail(TransactionCreatedEvent event) {
        log.info("Processando envio de e-mail para transação: transactionId={}, asset={}", event.transactionId(), event.assetSymbol());
        send(event.transactionId(), event.assetSymbol(), event.type(), event.quantity(), event.unitPrice(), event.totalValue(), event.createdAt(), defaultRecipient);
    }

    public void sendEmail(TransactionCreatedDto dto) {
        log.info("Processando envio de e-mail para transação (DTO): transactionId={}, asset={}", dto.transactionId(), dto.assetSymbol());
        send(dto.transactionId(), dto.assetSymbol(), dto.type(), dto.quantity(), dto.unitPrice(), dto.totalValue(), dto.createdAt(), defaultRecipient);
    }

    public void sendEmail(TransactionCreatedEvent event, String recipient) {
        log.info("Processando envio de e-mail para transação: transactionId={}, recipient={}", event.transactionId(), recipient);
        send(event.transactionId(), event.assetSymbol(), event.type(), event.quantity(), event.unitPrice(), event.totalValue(), event.createdAt(), recipient);
    }

    public void sendEmail(TransactionCreatedDto dto, String recipient) {
        log.info("Processando envio de e-mail para transação (DTO): transactionId={}, recipient={}", dto.transactionId(), recipient);
        send(dto.transactionId(), dto.assetSymbol(), dto.type(), dto.quantity(), dto.unitPrice(), dto.totalValue(), dto.createdAt(), recipient);
    }

    public void send(TransactionCreatedEvent event) {
        sendEmail(event);
    }

    public void send(TransactionCreatedDto dto) {
        sendEmail(dto);
    }

    private void send(UUID transactionId, String assetSymbol, TransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalValue, LocalDateTime createdAt, String recipient) {
        try {
            Context context = new Context();
            context.setVariable("transactionId", transactionId);
            context.setVariable("assetSymbol", assetSymbol);
            context.setVariable("type", type);
            context.setVariable("quantity", quantity);
            context.setVariable("unitPrice", unitPrice);
            context.setVariable("totalValue", totalValue);
            context.setVariable("createdAt", createdAt);

            String htmlContent = templateEngine.process("transaction-created", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            String sender = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "no-reply@wallet.com";
            String targetRecipient = (recipient != null && !recipient.isBlank()) ? recipient : defaultRecipient;

            helper.setFrom(sender);
            helper.setTo(targetRecipient);
            helper.setSubject("Notificação de Transação - Wallet");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("E-mail de confirmação de transação enviado com sucesso para {}", targetRecipient);
        } catch (MessagingException | MailException e) {
            log.error("Erro ao enviar e-mail de transação para {}: {}", recipient, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail para: " + recipient, e);
        }
    }
}
