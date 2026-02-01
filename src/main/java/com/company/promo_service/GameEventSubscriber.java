package com.company.promo_service;

import com.company.promobridge.GameEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

@RequiredArgsConstructor
public class GameEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(GameEventSubscriber.class);
    private final GameEventHandler gameEventHandler;

    @KafkaListener(topics = "${game-events.topic}", groupId = "promo-service", concurrency = "${game-events.concurrency}")
    public void onGameEvent(GameEvent event) {
        logger.info("Received game event {}{}", event.gameId(), event);
        gameEventHandler.handleEvent(event);
    }
}
