package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class LeaderboardService {

    public static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    private final TournamentEventService tournamentEventService;
    private final ScoreService scoreService;
    private final LeaderboardEventPublisher eventPublisher;


    @Scheduled(fixedRate = 10000)
    public void updateLeaderboard() {

        if (this.tournamentEventService.getActiveTournamentIds().isEmpty()) {
            logger.debug("There is no active tournament on the list: {}", tournamentEventService.getActiveTournamentIds());
        } else {
            List<String> tournamentIds = tournamentEventService.getActiveTournamentIds()
                    .stream()
                    .toList();

            for (String currentTournamentId : tournamentIds) {
                List<PlayerScore> scores = scoreService.getTournamentScores(currentTournamentId);
                int totalPlayers = tournamentEventService.getCurrentTournament(currentTournamentId).totalPlayers();
                buildLeaderboard(currentTournamentId, scores, totalPlayers);
            }
        }
    }

    private void buildLeaderboard(String currentTournamentId, List<PlayerScore> scores, int totalPlayers) {

        LeaderboardEvent leaderboardEvent = LeaderboardEvent
                .builder()
                .tournamentId(currentTournamentId)
                .timestamp(Timestamp.from(Instant.now()))
                .playersCount(totalPlayers)
                .averageScore(buildAverageScore(scores))
                .topPlayers(buildTopThreeRanking(scores))
                .build();

        eventPublisher.publishLeaderboardUpdated(currentTournamentId, leaderboardEvent);
        logger.info("leaderboard {} with current tournament id {}" +
                " has been successfully published", leaderboardEvent, currentTournamentId);

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
