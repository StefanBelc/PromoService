package com.company.promo.service.leaderboard;

import lombok.Builder;

@Builder
public record LeaderboardEntryDto(String name, int score, int rank) {
}
