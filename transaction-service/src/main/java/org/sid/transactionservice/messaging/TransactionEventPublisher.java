package org.sid.transactionservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * Producteur Kafka : envoie l'événement TransactionCreated uniquement après le commit de la transaction
 * en base (persistance réussie). La clé du message est l'accountId : tous les événements d'un même compte
 * vont dans la même partition et restent donc ordonnés.
 */
@Slf4j
@Component
public class TransactionEventPublisher {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public TransactionEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     @Value("${bank.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        String key = event.accountId().toString();
        String payload = JSON.writeValueAsString(event);
        try {
            kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Échec de publication de l'événement {} (transaction {}) : {}",
                            event.eventId(), event.transactionId(), ex.getMessage());
                } else {
                    log.info("Événement {} publié dans {} [clé={}, partition={}, offset={}]",
                            event.eventType(), topic, key,
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException ex) {
            // Kafka indisponible : la transaction bancaire est déjà validée, on journalise seulement l'erreur
            log.error("Kafka indisponible, événement {} non publié : {}", event.eventId(), ex.getMessage());
        }
    }
}
