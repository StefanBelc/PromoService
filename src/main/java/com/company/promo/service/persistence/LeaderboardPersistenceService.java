package com.company.promo.service.persistence;

import com.company.promo.service.leaderboard.messaging.LeaderboardEvent;
import com.company.promo.service.persistence.entity.LeaderboardEntity;
import com.company.promo.service.persistence.entity.LeaderboardTopPlayerEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardPersistenceService {

    private final LeaderboardTopPlayersRepository leaderboardTopPlayersRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public void persistLeaderboardSnapshot(LeaderboardEvent leaderboardEvent) {
        LeaderboardEntity leaderboardEntity = new LeaderboardEntity();
        leaderboardEntity.setTournamentId(leaderboardEvent.tournamentId());
        leaderboardEntity.setPlayersCount(leaderboardEvent.playersCount());
        leaderboardEntity.setAverageScore(leaderboardEvent.averageScore());
        leaderboardEntity.setTimestamp(leaderboardEvent.timestamp());

        leaderboardRepository.save(leaderboardEntity);
        log.info("leaderboard saved to database at {} with tournament id {}", leaderboardEvent.timestamp(), leaderboardEvent.tournamentId());


        if (leaderboardEvent.topPlayers() != null) {
            leaderboardEvent.topPlayers()
                    .forEach(playerScore -> {
                        LeaderboardTopPlayerEntity topPlayer = new LeaderboardTopPlayerEntity();
                        topPlayer.setTournamentId(leaderboardEvent.tournamentId());
                        topPlayer.setName(playerScore.playerName());
                        topPlayer.setScore(playerScore.score());
                        leaderboardTopPlayersRepository.save(topPlayer);
                        log.info("top players saved to database at {} with tournament id {}", leaderboardEvent.timestamp(), leaderboardEvent.tournamentId());
                    });
        }
    }

}
