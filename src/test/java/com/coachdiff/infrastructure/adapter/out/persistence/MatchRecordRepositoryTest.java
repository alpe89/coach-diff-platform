package com.coachdiff.infrastructure.adapter.out.persistence;

import static com.coachdiff.testutil.TestFixtures.createMatchRecordEntity;
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
    var record = createMatchRecordEntity("EUW1_7654321", "some-puuid-12345");

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
}
