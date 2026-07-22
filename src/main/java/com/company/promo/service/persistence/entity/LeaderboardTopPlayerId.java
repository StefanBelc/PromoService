package com.company.promo.service.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class LeaderboardTopPlayerId implements Serializable {

    private String tournamentId;
    private String name;


    public LeaderboardTopPlayerId() {
    }


    public LeaderboardTopPlayerId(String tournamentId, String name) {
        this.tournamentId = tournamentId;
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardTopPlayerId that = (LeaderboardTopPlayerId) o;
        return Objects.equals(tournamentId, that.tournamentId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournamentId, name);
    }

}


