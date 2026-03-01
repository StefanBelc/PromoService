package com.company.promo.service.tournament;

import com.company.promobridge.TournamentStatus;
import lombok.Builder;

@Builder
public record Tournament(String tournamentId, TournamentStatus status, int totalPlayers) {
}
