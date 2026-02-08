package com.company.promo_service;

import com.company.promobridge.TournamentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

@RequiredArgsConstructor
public class TournamentEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(TournamentEventSubscriber.class);
    private final TournamentEventService tournamentEventService;

    @KafkaListener(topics = "${tournament-events.topic}", groupId = "promo-service", concurrency = "${tournament-events.concurrency}")
    public void onTournamentEvent(TournamentEvent tournamentEvent) {
        logger.info("Received tournament event {}{} ", tournamentEvent.tournamentId(), tournamentEvent);
        tournamentEventService.handleEvent(tournamentEvent);
    }
}
