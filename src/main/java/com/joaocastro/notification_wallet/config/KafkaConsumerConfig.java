package com.joaocastro.notification_wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public RecordMessageConverter recordMessageConverter(ObjectMapper objectMapper) {
        StringJsonMessageConverter converter = new StringJsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("*");
        converter.setTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public CommonErrorHandler errorHandler() {
        return new DefaultErrorHandler((record, exception) -> {
            log.error("Erro irrecuperável ao processar registro do Kafka no tópico '{}', partição {}, offset {}: {}",
                    record.topic(), record.partition(), record.offset(), exception.getMessage(), exception);
        }, new FixedBackOff(1000L, 2L));
    }
}
