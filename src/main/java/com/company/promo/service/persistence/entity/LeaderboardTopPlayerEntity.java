package com.company.promo.service.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leaderboard_top_players")
@Getter
@Setter
@IdClass(LeaderboardTopPlayerId.class)
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardTopPlayerEntity {

    @Id
    @Column(name = "tournament_id")
    private String tournamentId;

    @Id
    @Column(name = "player_name")
    private String name;

    @Column(name = "player_score")
    private long score;
}
