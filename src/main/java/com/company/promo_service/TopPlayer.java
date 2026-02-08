package com.company.promo_service;

import lombok.Builder;

@Builder
public record TopPlayer(int rank, String playerName, int score) {
}
