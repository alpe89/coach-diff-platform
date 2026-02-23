package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Match;
import java.util.List;

public interface SaveMatchRecordsPort {
  void saveMatchRecords(List<Match> matches);
}
