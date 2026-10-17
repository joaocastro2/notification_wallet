package com.joaocastro.notification_wallet.strategy;

import com.joaocastro.notification_wallet.dto.TransactionCreatedDto;
import com.joaocastro.notification_wallet.dto.UserCreatedDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailStrategyConfig {

    @Bean
    public EmailStrategy<UserCreatedDto> userCreatedDtoEmailStrategy(UserCreatedEmailStrategy strategy) {
        return strategy::sendEmail;
    }

    @Bean
    public EmailStrategy<TransactionCreatedDto> transactionCreatedDtoEmailStrategy(AssetTransactionEmailStrategy strategy) {
        return strategy::sendEmail;
    }
}
