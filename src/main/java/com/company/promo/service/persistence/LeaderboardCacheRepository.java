package com.company.promo.service.persistence;

import com.company.promo.service.leaderboard.endpoint.LeaderboardEntry;
import com.company.promo.service.player.score.PlayerScore;

import java.util.List;

public interface LeaderboardCacheRepository {

    void save(PlayerScore player);

    void updateScore(PlayerScore player, int score);

    void remove(PlayerScore player);

    long getRank(PlayerScore player);

    List<LeaderboardEntry> getLeaderboard(String tournamentId);

    List<LeaderboardEntry> getTop(String tournamentId, int limit);

}
