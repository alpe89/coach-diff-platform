package com.coachdiff.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiotTimelineDTO(Metadata metadata, Info info) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Metadata(List<String> participants) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Info(long frameInterval, List<Frame> frames) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Frame(long timestamp, Map<String, ParticipantFrame> participantFrames) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ParticipantFrame(
      int minionsKilled, int jungleMinionsKilled, int totalGold, int xp) {}
}
