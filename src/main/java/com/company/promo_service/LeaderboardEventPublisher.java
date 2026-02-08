package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaderboardEventPublisher {

    private final KafkaTemplate<String, LeaderboardEvent> kafkaTemplate;

    @Value("${myapp.kafka.leaderboard-events.topic}")
    private final String leaderboardEventTopic;


    public void publishLeaderboardUpdated(String eventId, LeaderboardEvent leaderboardEvent) {
        kafkaTemplate.send(leaderboardEventTopic, eventId, leaderboardEvent);
    }
}
