package com.coachdiff.infrastructure.adapter.out.riot;

import com.coachdiff.domain.port.out.FetchRiotAccountPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RiotAccountAdapter implements FetchRiotAccountPort {
  private final RiotAccountClient riotAccountClient;

  RiotAccountAdapter(RiotAccountClient riotAccountClient) {
    this.riotAccountClient = riotAccountClient;
  }

  @Override
  public Optional<String> getPuuid(String name, String tag) {
    return riotAccountClient.getRiotAccountPuuid(name, tag);
  }
}
