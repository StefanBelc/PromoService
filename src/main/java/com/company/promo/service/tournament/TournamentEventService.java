package com.company.promo.service.tournament;

import com.company.promobridge.TournamentEvent;
import com.company.promobridge.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentEventService {


    private static final Logger logger = LoggerFactory.getLogger(TournamentEventService.class);
    private final Map<String, Tournament> activeTournaments;


    public void handleEvent(TournamentEvent event) {
        if (event.tournamentStatus().equals(TournamentStatus.STARTED)) {
            activeTournaments.put(event.tournamentId(), Tournament.builder()
                    .tournamentId(event.tournamentId())
                    .status(event.tournamentStatus())
                    .totalPlayers(event.totalPlayers())
                    .build());
            logger.info("Tournament with id {} is active", event.tournamentId());
        }
    }


    public Set<String> getActiveTournamentIds() {
        return this.activeTournaments.keySet();
    }

    public Tournament getCurrentTournament(String tournamentId) {
        return this.activeTournaments.get(tournamentId);
    }
}

