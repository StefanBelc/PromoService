package com.company.promo_service;

import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record LeaderboardEvent(String tournamentId,
                               Timestamp timestamp,
                               int playersCount,
                               double averageScore,
                               List<TopPlayer> topPlayers) {
}
