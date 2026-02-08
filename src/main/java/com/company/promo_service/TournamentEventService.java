package com.company.promo_service;

import com.company.promobridge.TournamentEvent;
import com.company.promobridge.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentEventService {


    private static final Logger logger = LoggerFactory.getLogger(TournamentEventService.class);
    private final List<String> activeTournaments;



    public void handleEvent(TournamentEvent event) {
        if(event.tournamentStatus().equals(TournamentStatus.STARTED)) {
            activeTournaments.add(event.tournamentId());
            logger.info("Tournament with id {} is active",event.tournamentId());
        }
    }



    public List<String> getActiveTournamentIds() {
        return this.activeTournaments;
    }
}

