package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.Account;

public interface FetchAccountPort {
  Account loadAccount(String email);
}
