package com.company.promo.service;

import com.company.promo.service.leaderboard.LeaderboardController;
import com.company.promo.service.leaderboard.LeaderboardDto;
import com.company.promo.service.leaderboard.LeaderboardEntryDto;
import com.company.promo.service.leaderboard.LeaderboardService;
import com.company.promo.service.tournament.TournamentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaderboardController.class)
@AutoConfigureWebMvc
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMVc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void should_returnLeaderboard_with_topNPlayers() throws Exception {

        LeaderboardDto expectedLeaderboard = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Mihai", 10, 1),
                new LeaderboardEntryDto("Alex", 8, 2),
                new LeaderboardEntryDto("Florin", 7, 3)
        ));

        when(leaderboardService.getTop("tournament1", 3)).thenReturn(expectedLeaderboard);

        MvcResult result = mockMVc.perform(get("/tournament/tournament1/leaderboard/top/3"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LeaderboardDto actualLeaderboard = objectMapper.readValue(response, LeaderboardDto.class);

        assertThat(actualLeaderboard)
                .usingRecursiveComparison()
                .isEqualTo(expectedLeaderboard);


    }

    @Test
    void should_return404_when_TournamentNotFound() throws Exception {
        when(leaderboardService.getTop("invalid", 2)).thenThrow(new TournamentNotFoundException("tournament not found"));

        mockMVc.perform(get("/tournament/invalid/leaderboard/top/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Tournament not found"))
                .andExpect(jsonPath("$.message").value("tournament not found"));
    }


    @Test
    void should_returnLeaderboard() throws Exception {

        LeaderboardDto expectedLeaderboard = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Mihai", 10, 1),
                new LeaderboardEntryDto("Alex", 8, 2),
                new LeaderboardEntryDto("Florin", 7, 3),
                new LeaderboardEntryDto("Andrei", 4, 4),
                new LeaderboardEntryDto("Elena", 2, 5)
        ));

        when(leaderboardService.getLeaderboard("tournament1")).thenReturn(expectedLeaderboard);

        MvcResult result = mockMVc.perform(get("/tournament/tournament1/leaderboard/all"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LeaderboardDto actualLeaderboard = objectMapper.readValue(response, LeaderboardDto.class);


        assertThat(actualLeaderboard)
                .usingRecursiveComparison()
                .isEqualTo(expectedLeaderboard);
    }
}