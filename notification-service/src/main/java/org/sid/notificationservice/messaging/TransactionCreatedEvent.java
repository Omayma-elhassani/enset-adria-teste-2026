package org.sid.notificationservice.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Événement reçu depuis le topic bank.transactions.created (même format que le producteur). */
public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt) {
}
