package com.company.promo_service;

import com.company.promobridge.TournamentStatus;
import lombok.Builder;

@Builder
public record Tournament(String tournamentId, TournamentStatus status, int totalPlayers) {
}
