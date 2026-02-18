package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class RedisLeaderboardRepository implements LeaderboardRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisLeaderboardRepository.class);
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(PlayerScore player) {
        redisTemplate.opsForZSet().add(player.tournamentId(), player.playerName(), player.score());
        logger.info("Player {}, saved successfully in Leaderboard", player.playerName());
    }

    @Override
    public void updateScore(PlayerScore player, int score) {
        redisTemplate.opsForZSet().incrementScore(player.tournamentId(), player.playerName(), score);
        logger.info("Incremented score {} for player {}", score, player.playerName());
    }

    @Override
    public void remove(PlayerScore player) {
        redisTemplate.opsForZSet().remove(player.tournamentId(), player.playerName());
        logger.info("Player {} removed successfully", player);
    }

    @Override
    public long getRank(PlayerScore player) {
        Long result = redisTemplate.opsForZSet().rank(player.tournamentId(), player.playerName());
        if (result == null) {
            logger.debug("Player rank for {} not found in leaderboard", player.playerName());
            throw new PlayerNotFoundException("Player not found in RedisLeaderboard");
        } else {
            logger.info("Player rank fetched successfully for player {}", player.playerName());
            return result;
        }
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(String tournamentId) {

        Set<ZSetOperations.TypedTuple<String>> redisReversedPlayerScoreSet = redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(tournamentId, 0, -1);
        return mapToLeaderboard(tournamentId, redisReversedPlayerScoreSet);

    }

    @Override
    public List<LeaderboardEntry> getTop(String tournamentId, int limit) {


        Set<ZSetOperations.TypedTuple<String>> reversedRangeWithScores = redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(tournamentId, 0, limit - 1);
        return mapToLeaderboard(tournamentId, reversedRangeWithScores);

    }


    private List<LeaderboardEntry> mapToLeaderboard(String tournamentId, Set<ZSetOperations.TypedTuple<String>> playerScoreSet) {
        if (playerScoreSet == null || playerScoreSet.isEmpty()) {
            return List.of();
        }

        List<ZSetOperations.TypedTuple<String>> playerScoresList = playerScoreSet
                .stream()
                .toList();


        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> entry : playerScoresList) {
            entries.add(LeaderboardEntry.builder()
                    .tournamentId(tournamentId)
                    .playerName(entry.getValue())
                    .score(entry.getScore().intValue())
                    .rank(rank++)
                    .build());
        }

        return entries;

    }


}
