package com.company.promo_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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


    @Test
    void getTop() throws Exception {

        LeaderboardDto leaderboardDto = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Mihai", 10, 1),
                new LeaderboardEntryDto("Alex", 8, 2),
                new LeaderboardEntryDto("Florin", 7, 3)
        ));

        when(leaderboardService.getTop("tournament1", 3)).thenReturn(leaderboardDto);

        mockMVc.perform(get("/tournament/tournament1/leaderboard/top/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboardEntryDtoList[0].name").value("Mihai"))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[1].score").value(8))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[2].rank").value(3));
    }


    @Test
    void getLeaderboard() throws Exception {

        LeaderboardDto leaderboardDto = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Mihai", 10, 1),
                new LeaderboardEntryDto("Alex", 8, 2),
                new LeaderboardEntryDto("Florin", 7, 3),
                new LeaderboardEntryDto("Andrei", 4, 4),
                new LeaderboardEntryDto("Elena", 2, 5)
        ));

        when(leaderboardService.getLeaderboard("tournament1")).thenReturn(leaderboardDto);

        mockMVc.perform(get("/tournament/tournament1/leaderboard/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboardEntryDtoList[0].name").value("Mihai"))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[1].score").value(8))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[2].rank").value(3))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[3].name").value("Andrei"))
                .andExpect(jsonPath("$.leaderboardEntryDtoList[4].rank").value(5));
    }
}