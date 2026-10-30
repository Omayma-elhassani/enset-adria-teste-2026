package org.sid.transactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sid.transactionservice.domain.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(
        @NotNull UUID accountId,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        TransactionType type,
        String merchantCategory,
        String country) {
}
