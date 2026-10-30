package org.sid.transactionservice.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Création automatique du topic bank.transactions.created (3 partitions) au démarrage. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionsCreatedTopic(@Value("${bank.kafka.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
