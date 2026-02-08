package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaderboardEventPublisher {

    private final KafkaTemplate<String,Map<String,Integer>> kafkaTemplate;

    @Value("${myapp.kafka.leaderboard-events.topic}")
    private final String leaderboardEventTopic;


    public void publishLeaderboardUpdated(String eventId, Map<String,Integer> event) {
        kafkaTemplate.send(leaderboardEventTopic,eventId, event);
    }
}
