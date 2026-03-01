package com.company.promo.service.player.score;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScoreService {


    private final ConcurrentHashMap<ScoreKey, PlayerScore> playerScores;
    private final static int WIN_SCORE = 3;
    private final static int DRAW_SCORE = 1;
    private final static int LOSS_SCORE = 0;

    public ScoreService() {
        this.playerScores = new ConcurrentHashMap<>();
    }


    public void incrementWin(String playerName, String tournamentId) {
        updatePlayerScore(playerName, WIN_SCORE, tournamentId);
    }

    public void incrementLoss(String playerName, String tournamentId) {
        updatePlayerScore(playerName, LOSS_SCORE, tournamentId);
    }

    public void incrementDraw(String playerName, String tournamentId) {
        updatePlayerScore(playerName, DRAW_SCORE, tournamentId);
    }


    public void updatePlayerScore(String name, int score, String tournamentId) {
        ScoreKey scoreKey = new ScoreKey(name, tournamentId);
        PlayerScore playerScore = this.playerScores.computeIfAbsent(scoreKey, key -> new PlayerScore(name, tournamentId, score));
        this.playerScores.put(scoreKey, new PlayerScore(name, tournamentId, playerScore.score() + score));

    }

    public List<PlayerScore> getTournamentScores(String tournamentId) {
        return playerScores.values()
                .stream()
                .filter(value -> value.tournamentId().equals(tournamentId))
                .toList();
    }
}
