package com.company.promo_service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("tournament/{tournamentId}/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;


    @GetMapping("/top/{n}")
    public LeaderboardDto getTop(@NotBlank @PathVariable String tournamentId,
                                 @Positive @PathVariable int n) {
        return leaderboardService.getTop(tournamentId, n);
    }


    @GetMapping("/all")
    public LeaderboardDto getLeaderboard(@NotBlank @PathVariable String tournamentId) {
        return leaderboardService.getLeaderboard(tournamentId);
    }
}
