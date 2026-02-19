package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.Profile;

public interface FetchProfilePort {
  Profile getProfile(String name, String tag);
}
