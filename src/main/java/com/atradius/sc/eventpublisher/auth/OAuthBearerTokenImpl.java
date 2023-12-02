package com.atradius.sc.eventpublisher.auth;

import java.util.Date;
import java.util.Set;
import lombok.Getter;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;

@Getter
public class OAuthBearerTokenImpl implements OAuthBearerToken {
  private final String token;
  private final long lifetimeMs;

  public OAuthBearerTokenImpl(final String token, Date expiresOn) {
    this.token = token;
    this.lifetimeMs = expiresOn.getTime();
  }

  public String value() {
    return this.token;
  }

  public Set<String> scope() {
    return null;
  }

  public long lifetimeMs() {
    return this.lifetimeMs;
  }

  public String principalName() {
    return null;
  }

  public Long startTimeMs() {
    return null;
  }
}
