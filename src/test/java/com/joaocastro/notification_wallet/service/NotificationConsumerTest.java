package com.joaocastro.notification_wallet.service;

import com.joaocastro.notification_wallet.dto.TransactionCreatedDto;
import com.joaocastro.notification_wallet.dto.TransactionCreatedEvent;
import com.joaocastro.notification_wallet.dto.TransactionType;
import com.joaocastro.notification_wallet.dto.UserCreatedDto;
import com.joaocastro.notification_wallet.dto.UserCreatedEvent;
import com.joaocastro.notification_wallet.strategy.AssetTransactionEmailStrategy;
import com.joaocastro.notification_wallet.strategy.UserCreatedEmailStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private UserCreatedEmailStrategy userCreatedEmailStrategy;

    @Mock
    private AssetTransactionEmailStrategy assetTransactionEmailStrategy;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    @DisplayName("Deve delegar UserCreatedEvent para UserCreatedEmailStrategy")
    void shouldDelegateUserCreatedEventToStrategy() {
        UserCreatedEvent event = new UserCreatedEvent(UUID.randomUUID(), "Alice", "alice@example.com", LocalDateTime.now());

        notificationConsumer.consumeUserCreated(event);

        verify(userCreatedEmailStrategy).sendEmail(event);
    }

    @Test
    @DisplayName("Deve delegar UserCreatedDto para UserCreatedEmailStrategy")
    void shouldDelegateUserCreatedDtoToStrategy() {
        UserCreatedDto dto = new UserCreatedDto(UUID.randomUUID(), "Bob", "bob@example.com", LocalDateTime.now());

        notificationConsumer.consumeUserCreated(dto);

        verify(userCreatedEmailStrategy).sendEmail(dto);
    }

    @Test
    @DisplayName("Deve delegar TransactionCreatedEvent para AssetTransactionEmailStrategy")
    void shouldDelegateTransactionCreatedEventToStrategy() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "PETR4",
                TransactionType.BUY,
                BigDecimal.TEN,
                BigDecimal.valueOf(35.50),
                BigDecimal.valueOf(355.00),
                LocalDateTime.now()
        );

        notificationConsumer.consumeAssetTransaction(event);

        verify(assetTransactionEmailStrategy).sendEmail(event);
    }

    @Test
    @DisplayName("Deve delegar TransactionCreatedDto para AssetTransactionEmailStrategy")
    void shouldDelegateTransactionCreatedDtoToStrategy() {
        TransactionCreatedDto dto = new TransactionCreatedDto(
                UUID.randomUUID(),
                "VALE3",
                TransactionType.SELL,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60.00),
                BigDecimal.valueOf(3000.00),
                LocalDateTime.now()
        );

        notificationConsumer.consumeAssetTransaction(dto);

        verify(assetTransactionEmailStrategy).sendEmail(dto);
    }

    @Test
    @DisplayName("Deve delegar consumeTransactionCreated para AssetTransactionEmailStrategy")
    void shouldDelegateConsumeTransactionCreatedAlias() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "ITUB4",
                TransactionType.BUY,
                BigDecimal.ONE,
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(30.00),
                LocalDateTime.now()
        );

        notificationConsumer.consumeTransactionCreated(event);

        verify(assetTransactionEmailStrategy).sendEmail(event);
    }
}
