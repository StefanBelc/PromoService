package com.company.promo.service;

import com.company.promo.service.leaderboard.*;
import com.company.promo.service.player.score.ScoreService;
import com.company.promo.service.tournament.Tournament;
import com.company.promo.service.tournament.TournamentEventService;
import com.company.promo.service.tournament.TournamentNotFoundException;
import com.company.promobridge.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "promo.service.leaderboard.update.rate.ms=500"
        })

@TestPropertySource(properties = {"myapp.kafka.topic.game-events=game.events",
        "myapp.kafka.topic.tournament-events=tournament.events",
        "spring.kafka.listener.auto-startup=false"})
@Testcontainers
@AutoConfigureMockMvc
public class LeaderboardControllerIntegrationTest {


    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);


    @MockitoBean
    private LeaderboardEventPublisher leaderboardEventPublisher;

    @MockitoBean
    private KafkaTemplate<String, LeaderboardEvent> kafkaTemplate;

    @Autowired
    GameEventProducer gameEventProducer;


    @Autowired
    ScoreService scoreService;

    @Autowired
    private RedisLeaderboardRepository redisLeaderboardRepository;

    @Autowired
    private LeaderboardService leaderboardService;
    @Autowired
    TournamentEventService tournamentEventService;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }


    @Test
    void should_save_and_returnLeaderboard_with_topNPlayer() throws InterruptedException {

        LeaderboardDto expectedLeaderboard = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Alex", 10, 1),
                new LeaderboardEntryDto("Mihai", 8, 2),
                new LeaderboardEntryDto("Florin", 7, 3)
        ));

        scoreService.updatePlayerScore("Mihai", 8, "tournament1");
        scoreService.updatePlayerScore("Alex", 10, "tournament1");
        scoreService.updatePlayerScore("Florin", 7, "tournament1");

        handleTournamentEvent();
        Thread.sleep(2000);
        leaderboardService.updateLeaderboard();

        LeaderboardDto actualLeaderboard =
                given()
                        .when()
                        .get("/tournament/{tournamentId}/leaderboard/top/{n}", "tournament1", 3)
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(LeaderboardDto.class);


        assertThat(actualLeaderboard).
                usingRecursiveComparison()
                .isEqualTo(expectedLeaderboard);

        // TODO: PA-33 finish concurrent updates and empty list

    }

    private void handleTournamentEvent() {
        List<GameEvent> gameResults = Collections.synchronizedList(new ArrayList<>());
        gameResults.add(new GameEvent("game123",
                "tournament1",
                GameStatus.FINISHED,
                "Mihai",
                "Alex",
                null,
                null,
                true,
                null));

        TournamentEvent event = new TournamentEvent("tournament1",
                TournamentStatus.STARTED,
                2,
                4,
                null,
                null,
                gameResults);

        tournamentEventService.handleEvent(event);
    }

    @Test
    void should_throw_TournamentNotFoundException() {
        assertThrows(TournamentNotFoundException.class, () ->
                leaderboardService.getLeaderboard("tournament1"));
    }

    @Test
    void should_return_leaderboard_with_ties() throws InterruptedException {
        LeaderboardDto expectedLeaderboard = new LeaderboardDto(List.of(
                new LeaderboardEntryDto("Mihai", 10, 1),
                new LeaderboardEntryDto("Alex", 10, 2)
        ));

        scoreService.updatePlayerScore("Florin", 7, "tournament1");
        scoreService.updatePlayerScore("Mihai", 10, "tournament1");
        scoreService.updatePlayerScore("Alex", 10, "tournament1");

        handleTournamentEvent();
        Thread.sleep(2000);
        leaderboardService.updateLeaderboard();

        Response response = given()
                .when()
                .get("/tournament/{tournamentId}/leaderboard/top/{n}", "tournament1", 2);

        LeaderboardDto actualLeaderboard = response
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(LeaderboardDto.class);

        assertThat(actualLeaderboard)
                .usingRecursiveComparison()
                .isEqualTo(expectedLeaderboard);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ConcurrentHashMap<String, Tournament> activeTournaments() {
            return new ConcurrentHashMap<>();
        }
    }

    @Test
    void should_handle_concurrent_score_updates() throws InterruptedException {
        final String playerId = "ConcurrentPlayer";
        final String tournamentId = "tournament1";
        final int numberOfThreads = 10;
        final int updatesPerThread = 50;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < updatesPerThread; j++) {
                        scoreService.updatePlayerScore(playerId, 1, tournamentId);
                        handleTournamentEvent();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        int expectedScore = numberOfThreads * updatesPerThread;
        leaderboardService.updateLeaderboard();

        LeaderboardDto actualLeaderboard = given()
                .when()
                .get("/tournament/{tournamentId}/leaderboard/top/{n}", tournamentId, 1)
                .then()
                .log().all()
                .statusCode(200)
                .extract().as(LeaderboardDto.class);

        assertThat(actualLeaderboard.leaderboardEntryDtoList().get(0).score())
                .as("final score should reflect all changes")
                .isEqualTo(expectedScore);
    }

}

