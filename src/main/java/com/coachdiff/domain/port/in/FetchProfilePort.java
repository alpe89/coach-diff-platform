package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.SummonerProfile;

public interface FetchProfilePort {
  SummonerProfile getSummonerProfile(String name, String tag);
}
