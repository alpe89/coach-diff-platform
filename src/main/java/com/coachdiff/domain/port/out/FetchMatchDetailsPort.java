package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Match;
import java.util.List;

public interface FetchMatchDetailsPort {
  List<String> getMatchIdsByPuuid(String puuid);

  List<Match> getMatchRecords(String puuid, List<String> matchIds);
}
