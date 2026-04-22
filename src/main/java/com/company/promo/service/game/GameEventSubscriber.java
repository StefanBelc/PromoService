package com.company.promo.service.game;

import com.company.promobridge.GameEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(GameEventSubscriber.class);
    private final GameEventService gameEventService;

    @KafkaListener(topics = "${myapp.kafka.game-events.topic}", groupId = "promo-service", concurrency = "${myapp.kafka.game-events.concurrency}")
    public void onGameEvent(GameEvent event) {
        logger.info("Received game event {}{}", event.gameId(), event);
        gameEventService.handleEvent(event);
    }
}
