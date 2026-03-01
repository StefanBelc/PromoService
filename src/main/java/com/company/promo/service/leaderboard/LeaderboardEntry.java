package com.company.promo.service.leaderboard;

import lombok.Builder;

@Builder
public record LeaderboardEntry(String tournamentId, String playerName, int score, int rank) {
}
