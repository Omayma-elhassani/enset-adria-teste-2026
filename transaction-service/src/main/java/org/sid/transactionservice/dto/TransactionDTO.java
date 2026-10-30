package org.sid.transactionservice.dto;

import org.sid.transactionservice.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionDTO(UUID id, UUID accountId, BigDecimal amount, String currency, TransactionType type,
                             String merchantCategory, String country, Instant createdAt) {
}
