package com.coachdiff.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Testcontainers
class MatchRecordRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private MatchRecordRepository repository;

  @Test
  void shouldLoadStoredMatchRecord() {
    // arrange
    var record = createMatchRecord("EUW1_7654321", "some-puuid-12345");

    // act
    repository.save(record);

    // assert
    var foundRecord = repository.findById(record.getId());
    assertThat(foundRecord).isPresent();
    assertThat(foundRecord.get()).isEqualTo(record);
    assertThat(foundRecord.get().getGoldAt10()).isEqualTo(3200);
    assertThat(foundRecord.get().getDamagePerMinute()).isEqualTo(850);
    assertThat(foundRecord.get().getKda()).isEqualTo(4.5);
    assertThat(foundRecord.get().getSoloKills()).isEqualTo(2);
  }

  private MatchRecordEntity createMatchRecord(String matchId, String puuid) {
    return new MatchRecordEntity(
        matchId,
        puuid,
        true,
        32.5,
        // Combat
        8,
        3,
        12,
        4.5,
        2,
        850.0,
        1.2,
        0.28,
        0.18,
        0.65,
        // Economy
        420.0,
        7.8,
        // Objectives
        4500,
        12000,
        3,
        // Vision
        1.5,
        15,
        8,
        4,
        // Timeline
        78.0,
        3200.0,
        5800.0,
        6100.0);
  }
}
