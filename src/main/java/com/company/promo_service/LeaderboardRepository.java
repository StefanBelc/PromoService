package com.company.promo_service;

import java.util.List;

public interface LeaderboardRepository {

    void save(PlayerScore player);

    void updateScore(PlayerScore player, int score);

    void remove(PlayerScore player);

    long getRank(PlayerScore player);

    List<LeaderboardEntry> getLeaderboard(String tournamentId);

    List<LeaderboardEntry> getTop(String tournamentId, int limit);

}
