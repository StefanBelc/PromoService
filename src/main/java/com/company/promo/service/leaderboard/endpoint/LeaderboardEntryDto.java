package com.company.promo.service.leaderboard.endpoint;

import lombok.Builder;

@Builder
public record LeaderboardEntryDto(String name, int score, int rank) {
}
