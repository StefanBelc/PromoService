package com.company.promo.service.leaderboard.messaging;

import com.company.promo.service.leaderboard.TopPlayer;
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
