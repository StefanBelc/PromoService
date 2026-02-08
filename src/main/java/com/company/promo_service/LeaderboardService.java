package com.company.promo_service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class LeaderboardService {

    private ConcurrentHashMap<String, Integer> leaderboard;
    private final TournamentEventService tournamentEventService;
    private final ScoreService scoreService;
    private final LeaderboardEventPublisher eventPublisher;


    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(leaderboard);
    }


    @Scheduled(fixedRate = 10000)
    public void updateLeaderboard() {

        if (!this.tournamentEventService.getActiveTournamentIds().isEmpty()) {


            for (int i = 0; i < tournamentEventService.getActiveTournamentIds().size(); i++) {

                String currentTournamentId = tournamentEventService.getActiveTournamentIds().get(i);
                List<PlayerScore> scores = scoreService.getTournamentScores(currentTournamentId);
                buildRanking(scores);
                eventPublisher.publishLeaderboardUpdated(currentTournamentId, leaderboard);
            }
        }

    }

    private void buildRanking(List<PlayerScore> scores) {

        for (PlayerScore playerScore : scores) {
            leaderboard.putIfAbsent(playerScore.playerName(), playerScore.score());
        }

        leaderboard = this.leaderboard.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> newValue,
                        ConcurrentHashMap::new));
    }


}
