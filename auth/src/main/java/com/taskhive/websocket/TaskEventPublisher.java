package com.taskhive.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final SimpMessagingTemplate messaging;

    private static final String TOPIC = "/topic/tasks";

    public void publish(TaskEvent event) {
        messaging.convertAndSend(TOPIC, event);
    }
}
