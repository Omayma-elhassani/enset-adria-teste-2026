package org.sid.notificationservice.web;

import lombok.RequiredArgsConstructor;
import org.sid.notificationservice.messaging.ReceivedEvent;
import org.sid.notificationservice.messaging.TransactionEventConsumer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lecture seule : derniers événements reçus depuis Kafka (les plus récents en premier). */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final TransactionEventConsumer consumer;

    @GetMapping("/events")
    public List<ReceivedEvent> events() {
        return consumer.lastEvents();
    }
}
