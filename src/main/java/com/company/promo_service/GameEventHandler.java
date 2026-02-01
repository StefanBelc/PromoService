package com.company.promo_service;

import com.company.promobridge.GameEvent;
import com.company.promobridge.GameStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GameEventHandler {

    private final ScoreService scoreService;
    private static final Logger logger = LoggerFactory.getLogger(GameEventHandler.class);


    public void handleEvent(GameEvent event) {
        if (event.status() == GameStatus.FINISHED) {
            if (event.draw()) {
                scoreService.incrementDraw(event.player1(), event.tournamentId());
                logger.info("Player {} incremented draw count by 1 point", event.player1());
                scoreService.incrementDraw(event.player2(),event.tournamentId());
                logger.info("Player {} incremented draw count by 1 point", event.player2());
            } else {
                scoreService.incrementWin(event.winner(),event.tournamentId());
                logger.info("Player {} incremented win count by 3 points.", event.winner());
                scoreService.incrementLoss(event.loser(),event.tournamentId());
                logger.info("Player {} incremented loss count by 0 points.", event.loser());

            }
        }
    }

}
