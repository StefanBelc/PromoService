package com.company.promo_service;

import lombok.Builder;

@Builder
public record LeaderboardEntry(String tournamentId, String playerName, int score, int rank) {
}
