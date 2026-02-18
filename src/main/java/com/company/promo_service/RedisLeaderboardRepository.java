package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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

        Set<ZSetOperations.TypedTuple<String>> fullTupleSetRange = redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(tournamentId, 0, -1);
        return mapToLeaderboard(tournamentId, fullTupleSetRange);

    }

    @Override
    public List<LeaderboardEntry> getTop(String tournamentId, int limit) {


        Set<ZSetOperations.TypedTuple<String>> tupleSetRange = redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(tournamentId, 0, limit - 1);
        return mapToLeaderboard(tournamentId, tupleSetRange);

    }


    private List<LeaderboardEntry> mapToLeaderboard(String tournamentId, Set<ZSetOperations.TypedTuple<String>> tupleSet) {
        if (tupleSet == null || tupleSet.isEmpty()) {
            return List.of();
        }

        List<ZSetOperations.TypedTuple<String>> tupleList = tupleSet
                .stream()
                .toList();


        return IntStream.range(0, tupleList.size())
                .mapToObj(rank -> {
                    ZSetOperations.TypedTuple<String> entry = tupleList.get(rank);
                    return LeaderboardEntry
                            .builder()
                            .tournamentId(tournamentId)
                            .playerName(entry.getValue())
                            .score(entry.getScore().intValue())
                            .rank(rank + 1)
                            .build();
                })
                .toList();

    }


}
