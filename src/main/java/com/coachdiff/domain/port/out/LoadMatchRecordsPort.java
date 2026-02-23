package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Match;
import java.util.List;

public interface LoadMatchRecordsPort {

  List<Match> loadExistingMatchRecords(String puuid, List<String> matchIds);
}
