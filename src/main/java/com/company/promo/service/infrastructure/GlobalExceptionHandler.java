package com.company.promo.service.infrastructure;

import com.company.promo.service.player.PlayerNotFoundException;
import com.company.promo.service.tournament.TournamentNotFoundException;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TournamentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTournamentNotFound(TournamentNotFoundException exception) {
        return buildNotFoundResponse("Tournament not found", exception.getMessage());
    }


    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePlayerNotFound(PlayerNotFoundException exception) {
        return buildNotFoundResponse("Player not found", exception.getMessage());
    }

    @Nonnull
    private static ResponseEntity<Map<String, String>> buildNotFoundResponse(String error, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", error,
                        "message", message
                ));
    }
}
