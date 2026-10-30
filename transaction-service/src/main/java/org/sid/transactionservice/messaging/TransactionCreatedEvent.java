package org.sid.transactionservice.messaging;

import org.sid.transactionservice.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Événement publié dans le topic bank.transactions.created après la persistance d'une transaction.
 * Format : { eventId, eventType, transactionId, accountId, amount, currency, occurredAt }.
 */
public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt) {

    public static final String TYPE = "TransactionCreated";

    public static TransactionCreatedEvent from(Transaction t) {
        return new TransactionCreatedEvent(UUID.randomUUID(), TYPE, t.getId(), t.getAccountId(),
                t.getAmount(), t.getCurrency(), t.getCreatedAt());
    }
}
