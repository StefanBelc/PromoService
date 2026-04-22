package com.company.promo.service.tournament;

import com.company.promobridge.TournamentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TournamentEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(TournamentEventSubscriber.class);
    private final TournamentEventService tournamentEventService;

    @KafkaListener(topics = "${myapp.kafka.tournament-events.topic}", groupId = "promo-service", concurrency = "${myapp.kafka.tournament-events.concurrency}")
    public void onTournamentEvent(TournamentEvent tournamentEvent) {
        logger.info("Received tournament event {}{} ", tournamentEvent.tournamentId(), tournamentEvent);
        tournamentEventService.handleEvent(tournamentEvent);
    }
}
