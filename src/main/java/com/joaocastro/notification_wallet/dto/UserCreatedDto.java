package com.joaocastro.notification_wallet.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreatedDto(

        UUID userId,
        String userName,
        String userMail,
        LocalDateTime createdAt

) {
}
