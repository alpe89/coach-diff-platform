package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.MatchRecord;
import java.util.List;

public interface SaveMatchRecordsPort {
  void saveMatchRecords(List<MatchRecord> matchRecords);
}
