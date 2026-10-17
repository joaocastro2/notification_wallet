package com.joaocastro.notification_wallet.service;

import com.joaocastro.notification_wallet.dto.TransactionCreatedDto;
import com.joaocastro.notification_wallet.dto.TransactionCreatedEvent;
import com.joaocastro.notification_wallet.dto.UserCreatedDto;
import com.joaocastro.notification_wallet.dto.UserCreatedEvent;
import com.joaocastro.notification_wallet.strategy.AssetTransactionEmailStrategy;
import com.joaocastro.notification_wallet.strategy.UserCreatedEmailStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final UserCreatedEmailStrategy userCreatedEmailStrategy;
    private final AssetTransactionEmailStrategy assetTransactionEmailStrategy;

    @KafkaListener(
            topics = "${kafka.topics.user-created:user-created-topic}",
            groupId = "${spring.kafka.consumer.group-id:notification-group}"
    )
    public void consumeUserCreated(UserCreatedEvent event) {
        log.info("Mensagem recebida do tópico user-created: userId={}, email={}", event.userId(), event.userMail());
        userCreatedEmailStrategy.sendEmail(event);
    }

    public void consumeUserCreated(UserCreatedDto dto) {
        log.info("Mensagem DTO recebida para criação de usuário: userId={}, email={}", dto.userId(), dto.userMail());
        userCreatedEmailStrategy.sendEmail(dto);
    }

    @KafkaListener(
            topics = "${kafka.topics.transaction-created:transaction-created-topic}",
            groupId = "${spring.kafka.consumer.group-id:notification-group}"
    )
    public void consumeAssetTransaction(TransactionCreatedEvent event) {
        log.info("Mensagem recebida do tópico transaction-created: transactionId={}, asset={}", event.transactionId(), event.assetSymbol());
        assetTransactionEmailStrategy.sendEmail(event);
    }

    public void consumeAssetTransaction(TransactionCreatedDto dto) {
        log.info("Mensagem DTO recebida para transação de ativo: transactionId={}, asset={}", dto.transactionId(), dto.assetSymbol());
        assetTransactionEmailStrategy.sendEmail(dto);
    }

    public void consumeTransactionCreated(TransactionCreatedEvent event) {
        consumeAssetTransaction(event);
    }

    public void consumeTransactionCreated(TransactionCreatedDto dto) {
        consumeAssetTransaction(dto);
    }
}
