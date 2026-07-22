package com.company.promo.service.leaderboard;

import com.company.promo.service.leaderboard.endpoint.LeaderboardEntryDto;
import com.company.promo.service.leaderboard.messaging.LeaderboardEvent;
import com.company.promo.service.leaderboard.messaging.LeaderboardEventPublisher;
import com.company.promo.service.player.score.ScoreService;
import com.company.promo.service.tournament.TournamentEventService;
import com.company.promobridge.TournamentEvent;
import com.company.promobridge.TournamentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@Testcontainers
@Import(LeaderboardServiceIntegrationTest.TestKafkaConsumer.class)
class LeaderboardServiceIntegrationTest {


    @Container
    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0")
    );

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);


    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Autowired
    private StringRedisTemplate  stringRedisTemplate;


    @Autowired
    private LeaderboardEventPublisher leaderboardEventPublisher;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private TournamentEventService tournamentEventService;

    @Autowired
    private LeaderboardService leaderboardService;


    @Test
    void should_successfully_publish_event_with_scores() {
        String tournamentId = "tournament1";
        scoreService.incrementWin("player1", tournamentId);
        scoreService.incrementWin("player2", tournamentId);
        scoreService.incrementWin("player3", tournamentId);
        scoreService.incrementLoss("player2", tournamentId);
        scoreService.incrementLoss("player3", tournamentId);
        scoreService.incrementWin("player1", tournamentId);

        tournamentEventService.handleEvent(buildTournamentEvent());
        leaderboardService.updateLeaderboard();
        List<LeaderboardEntryDto> redisScores =leaderboardService.getLeaderboard(tournamentId).leaderboardEntryDtoList();
        await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    LeaderboardEvent event = testKafkaConsumer.getLatestEvent();
                    assertThat(event).isNotNull();
                    assertThat(event.tournamentId()).isEqualTo("tournament1");
                    assertThat(event.topPlayers().get(0).score()).isEqualTo(6);
                    assertThat(redisScores).isNotEmpty();
                    assertThat(redisScores.get(0).score()).isEqualTo(6);
                });


    }

    @Test
    void should_return_topPlayers_with_scores() {
        //TODO: FINISH TO IMPLEMENT
        String tournamentId = "tournament1";
        tournamentEventService.handleEvent(buildTournamentEvent());
        leaderboardEventPublisher.publishLeaderboardUpdated(tournamentId,buildLeaderboardEvent(tournamentId));
    }

    private TournamentEvent buildTournamentEvent() {
        return TournamentEvent
                .builder()
                .totalPlayers(4)
                .tournamentId("tournament1")
                .avgMatchDuration(Duration.of(20, ChronoUnit.SECONDS))
                .gameResults(new ArrayList<>())
                .matches(4)
                .totalDuration(Duration.of(40, ChronoUnit.SECONDS))
                .tournamentStatus(TournamentStatus.STARTED).build();
    }

    private LeaderboardEvent buildLeaderboardEvent(String tournamentId) {
        return  LeaderboardEvent
                .builder()
                .tournamentId(tournamentId)
                .timestamp(Timestamp.from(Instant.now()))
                .playersCount(7)
                .averageScore(4)
                .topPlayers(buildTopPlayers())
                .build();
    }

    private List<TopPlayer> buildTopPlayers() {
        return List.of(
                TopPlayer
                        .builder()
                        .rank(1)
                        .playerName("Mihai")
                        .score(3)
                        .build(),
                TopPlayer
                        .builder()
                        .rank(2)
                        .playerName("Andrei")
                        .score(2)
                        .build(),
                TopPlayer
                        .builder()
                        .rank(3)
                        .playerName("George")
                        .score(1)
                        .build());
    }


    @Component
    static class TestKafkaConsumer {

        private final List<LeaderboardEvent> consumedEvents = new CopyOnWriteArrayList<>();


        @KafkaListener(topics = "leaderboard.events",
                groupId = "unique-test-consumer-group",
                properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
        public void listen(LeaderboardEvent event) {
            System.out.println(event.toString());
            consumedEvents.add(event);
        }

        public LeaderboardEvent getLatestEvent() {
            if (consumedEvents.isEmpty()) {
                return null;
            }
            return consumedEvents.get(consumedEvents.size() - 1);
        }

        public void reset() {
            consumedEvents.clear();
        }
    }
}





