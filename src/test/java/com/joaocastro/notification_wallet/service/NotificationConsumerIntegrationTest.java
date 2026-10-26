package com.joaocastro.notification_wallet.service;

import com.joaocastro.notification_wallet.dto.TransactionCreatedEvent;
import com.joaocastro.notification_wallet.dto.TransactionType;
import com.joaocastro.notification_wallet.dto.UserCreatedEvent;
import com.joaocastro.notification_wallet.strategy.AssetTransactionEmailStrategy;
import com.joaocastro.notification_wallet.strategy.UserCreatedEmailStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "user-created-topic", "transaction-created-topic" })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class NotificationConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private UserCreatedEmailStrategy userCreatedEmailStrategy;

    @MockBean
    private AssetTransactionEmailStrategy assetTransactionEmailStrategy;

    @Test
    @DisplayName("Deve consumir UserCreatedEvent em formato JSON sem cabeçalhos (estilo auth_wallet)")
    void shouldConsumeUserCreatedEventWithoutHeaders() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaTemplate<String, String> rawTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        UUID userId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "userId": "%s",
                    "userName": "Carlos Silva",
                    "userMail": "carlos@example.com",
                    "createdAt": "2026-10-26T10:00:00"
                }
                """.formatted(userId);

        rawTemplate.send("user-created-topic", userId.toString(), jsonPayload);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ArgumentCaptor<UserCreatedEvent> captor = ArgumentCaptor.forClass(UserCreatedEvent.class);
            verify(userCreatedEmailStrategy).sendEmail(captor.capture());
            UserCreatedEvent captured = captor.getValue();
            assertThat(captured.userId()).isEqualTo(userId);
            assertThat(captured.userName()).isEqualTo("Carlos Silva");
            assertThat(captured.userMail()).isEqualTo("carlos@example.com");
        });
    }

    @Test
    @DisplayName("Deve consumir TransactionCreatedEvent em formato JSON com ou sem cabeçalhos (estilo wallet)")
    void shouldConsumeTransactionCreatedEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaTemplate<String, String> rawTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        UUID transactionId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "transactionId": "%s",
                    "assetSymbol": "PETR4",
                    "type": "BUY",
                    "quantity": 100.0,
                    "unitPrice": 37.50,
                    "totalValue": 3750.00,
                    "createdAt": "2026-10-26T10:30:00"
                }
                """.formatted(transactionId);

        rawTemplate.send("transaction-created-topic", "PETR4", jsonPayload);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ArgumentCaptor<TransactionCreatedEvent> captor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
            verify(assetTransactionEmailStrategy).sendEmail(captor.capture());
            TransactionCreatedEvent captured = captor.getValue();
            assertThat(captured.transactionId()).isEqualTo(transactionId);
            assertThat(captured.assetSymbol()).isEqualTo("PETR4");
            assertThat(captured.type()).isEqualTo(TransactionType.BUY);
            assertThat(captured.totalValue()).isEqualByComparingTo(BigDecimal.valueOf(3750.00));
        });
    }
}
