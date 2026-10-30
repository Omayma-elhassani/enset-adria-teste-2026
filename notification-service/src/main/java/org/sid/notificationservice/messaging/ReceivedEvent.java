package org.sid.notificationservice.messaging;

import java.time.Instant;

/** Événement reçu + métadonnées Kafka (clé, partition, offset), gardé en mémoire pour consultation. */
public record ReceivedEvent(String key, int partition, long offset, Instant receivedAt,
                            TransactionCreatedEvent event) {
}
