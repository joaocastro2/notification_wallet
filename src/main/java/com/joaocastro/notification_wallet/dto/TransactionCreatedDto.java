package com.joaocastro.notification_wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedDto(

        UUID transactionId,
        String assetSymbol,
        TransactionType type,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        LocalDateTime createdAt

) {
}
