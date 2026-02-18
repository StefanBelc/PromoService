package com.company.promo_service;

import lombok.Builder;

@Builder
public record LeaderboardEntryDto(String name, int score, int rank) {
}
