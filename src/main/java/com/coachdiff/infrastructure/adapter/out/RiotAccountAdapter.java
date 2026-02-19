package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.port.out.FetchAccountPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RiotAccountAdapter implements FetchAccountPort {
  private final RiotAccountClient riotAccountClient;

  RiotAccountAdapter(RiotAccountClient riotAccountClient) {
    this.riotAccountClient = riotAccountClient;
  }

  @Override
  public Optional<String> getPuuid(String name, String tag) {
    return riotAccountClient.getRiotAccountPuuid(name, tag);
  }
}
