package com.coachdiff.domain.port.out;

import java.util.Optional;

public interface FetchAccountPort {
  Optional<String> getPuuid(String name, String tag);
}
