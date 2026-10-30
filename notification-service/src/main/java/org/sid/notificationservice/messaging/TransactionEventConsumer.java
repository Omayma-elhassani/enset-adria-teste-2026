package org.sid.notificationservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Consumer des événements TransactionCreated.
 * Il se contente de journaliser l'événement reçu : aucune donnée bancaire n'est modifiée
 * (pas d'appel à account-service ni à transaction-service).
 */
@Slf4j
@Component
public class TransactionEventConsumer {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final int MAX_EVENTS = 100;

    private final Deque<ReceivedEvent> lastEvents = new ConcurrentLinkedDeque<>();

    @KafkaListener(topics = "${bank.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionCreated(ConsumerRecord<String, String> record) {
        try {
            TransactionCreatedEvent event = JSON.readValue(record.value(), TransactionCreatedEvent.class);
            log.info("[{}] eventId={} transactionId={} accountId={} montant={} {} occurredAt={} "
                            + "(clé={}, partition={}, offset={})",
                    event.eventType(), event.eventId(), event.transactionId(), event.accountId(),
                    event.amount(), event.currency(), event.occurredAt(),
                    record.key(), record.partition(), record.offset());
            lastEvents.addFirst(new ReceivedEvent(record.key(), record.partition(), record.offset(),
                    Instant.now(), event));
            while (lastEvents.size() > MAX_EVENTS) {
                lastEvents.removeLast();
            }
        } catch (JacksonException e) {
            log.warn("Message illisible ignoré (partition={}, offset={}) : {}",
                    record.partition(), record.offset(), record.value());
        }
    }

    public List<ReceivedEvent> lastEvents() {
        return new ArrayList<>(lastEvents);
    }
}
