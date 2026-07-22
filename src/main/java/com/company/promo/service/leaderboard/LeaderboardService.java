package com.company.promo.service.leaderboard;

import com.company.promo.service.leaderboard.endpoint.LeaderboardDto;
import com.company.promo.service.leaderboard.endpoint.LeaderboardEntry;
import com.company.promo.service.leaderboard.endpoint.LeaderboardEntryDto;
import com.company.promo.service.leaderboard.messaging.LeaderboardEvent;
import com.company.promo.service.leaderboard.messaging.LeaderboardEventPublisher;
import com.company.promo.service.persistence.LeaderboardPersistenceService;
import com.company.promo.service.persistence.RedisLeaderboardCacheRepository;
import com.company.promo.service.player.score.PlayerScore;
import com.company.promo.service.player.score.ScoreService;
import com.company.promo.service.tournament.TournamentEventService;
import com.company.promo.service.tournament.TournamentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LeaderboardService {

    public static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    private final TournamentEventService tournamentEventService;
    private final ScoreService scoreService;
    private final LeaderboardEventPublisher eventPublisher;
    private final RedisLeaderboardCacheRepository redisLeaderboardRepository;
    private final LeaderboardPersistenceService leaderboardPersistenceService;


    public LeaderboardDto getTop(String tournamentId, int limit) {
        if (isInvalidTournamentId(tournamentId)) {
            throw new TournamentNotFoundException("Tournament id does not exist as an active tournament");
        }

        List<LeaderboardEntry> topPlayersList = redisLeaderboardRepository.getTop(tournamentId, limit);

        List<LeaderboardEntryDto> leaderboardEntryDtoList = topPlayersList
                .stream()
                .map(entry -> LeaderboardEntryDto
                        .builder()
                        .name(entry.playerName())
                        .score(entry.score())
                        .rank(entry.rank())
                        .build())
                .toList();

        return new LeaderboardDto(leaderboardEntryDtoList);
    }

    public LeaderboardDto getLeaderboard(String tournamentId) {
        if (isInvalidTournamentId(tournamentId)) {
            throw new TournamentNotFoundException("Tournament id does not exist as an active tournament");
        }

        List<LeaderboardEntry> leaderboardList = redisLeaderboardRepository.getLeaderboard(tournamentId);

        List<LeaderboardEntryDto> leaderboardEntryDtoList = leaderboardList
                .stream()
                .map(entry -> LeaderboardEntryDto
                        .builder()
                        .name(entry.playerName())
                        .score(entry.score())
                        .rank(entry.rank())
                        .build())
                .toList();

        return new LeaderboardDto(leaderboardEntryDtoList);
    }

    private boolean isInvalidTournamentId(String tournamentId) {
        for (String activeTournamentId : getActiveTournamentIds()) {
            if (tournamentId.equals(activeTournamentId)) {
                return false;
            }
        }
        return true;
    }


    @Scheduled(fixedRate = 10000)
    public void updateLeaderboard() {
        List<String> tournamentIds = getActiveTournamentIds();

        for (String currentTournamentId : tournamentIds) {
            List<PlayerScore> scores = scoreService.getTournamentScores(currentTournamentId);
            if (scores == null || scores.isEmpty()) {
                logger.debug("score list is empty or null");
                continue;
            }
            int totalPlayers = tournamentEventService.getCurrentTournament(currentTournamentId).totalPlayers();
            LeaderboardEvent currentLeaderboard = buildLeaderboard(currentTournamentId, scores, totalPlayers);

            for (PlayerScore playerScore : scores) {
                logger.info("{} scores sent to redis leaderboard", playerScore.playerName());
                redisLeaderboardRepository.save(playerScore);
            }
            persistLeaderboard(currentLeaderboard);
            publishLeaderboardEvent(currentLeaderboard);
        }
    }

    private List<String> getActiveTournamentIds() {
        if (this.tournamentEventService.getActiveTournamentIds().isEmpty()) {
            logger.debug("There is no active tournament on the list: {}", tournamentEventService.getActiveTournamentIds());
            return List.of();
        } else {
            return tournamentEventService.getActiveTournamentIds()
                    .stream()
                    .toList();
        }
    }

    private void publishLeaderboardEvent(LeaderboardEvent leaderboardEvent) {

        eventPublisher.publishLeaderboardUpdated(leaderboardEvent.tournamentId(), leaderboardEvent);
        logger.info("leaderboard {} with current tournament id {}" +
                " has been successfully published", leaderboardEvent, leaderboardEvent.tournamentId());
    }


    private LeaderboardEvent buildLeaderboard(String currentTournamentId, List<PlayerScore> scores, int totalPlayers) {
        return LeaderboardEvent
                .builder()
                .tournamentId(currentTournamentId)
                .timestamp(Timestamp.from(Instant.now()))
                .playersCount(totalPlayers)
                .averageScore(buildAverageScore(scores))
                .topPlayers(buildTopThreeRanking(scores))
                .build();
    }


    private void persistLeaderboard(LeaderboardEvent leaderboardEvent) {
        leaderboardPersistenceService.persistLeaderboardSnapshot(leaderboardEvent);
    }


    private double buildAverageScore(List<PlayerScore> scores) {
        if (scores.isEmpty()) {
            return 0;
        } else {
            double averageScore = 0;
            for (PlayerScore score : scores) {
                averageScore += score.score();
            }
            return averageScore / scores.size();
        }
    }


    private List<TopPlayer> buildTopThreeRanking(List<PlayerScore> scores) {
        if (scores.isEmpty()) {
            return List.of();
        } else {
            List<PlayerScore> topThreePlayerScores = scores
                    .stream()
                    .sorted(Comparator.comparingInt(PlayerScore::score).reversed())
                    .limit(3)
                    .toList();

            List<TopPlayer> topThreePlayers = new ArrayList<>();
            for (int i = 0; i < topThreePlayerScores.size(); i++) {
                topThreePlayers.add(TopPlayer
                        .builder()
                        .rank(i + 1)
                        .playerName(topThreePlayerScores.get(i).playerName())
                        .score(topThreePlayerScores.get(i).score())
                        .build());
            }
            logger.info("top three ranking {} built successfully", topThreePlayers);
            return topThreePlayers;
        }
    }
}
