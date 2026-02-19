package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.MatchRecord;
import java.util.List;

public interface FetchMatchDetailsPort {
  List<String> getMatchIdsByPuuid(String puuid);

  List<MatchRecord> getMatchRecords(String puuid, List<String> matchIds);
}
