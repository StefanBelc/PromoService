package com.company.promo.service.leaderboard;

import lombok.Builder;

@Builder
public record TopPlayer(int rank, String playerName, int score) {
}
