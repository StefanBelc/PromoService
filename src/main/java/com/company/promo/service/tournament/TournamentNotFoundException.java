package com.company.promo.service.tournament;

public class TournamentNotFoundException extends RuntimeException {

    public TournamentNotFoundException(String message) {
        super(message);
    }
}
