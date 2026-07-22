package com.company.promo.service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;


@Entity
@Table(name = "leaderboard")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntity {

    @Id
    @Column(name = "tournament_id")
    private String tournamentId;

    @Column(name = "updated_at")
    private Timestamp timestamp;

    @Column(name = "players_count")
    private int playersCount;

    @Column(name = "avg_score")
    private double averageScore;

}
