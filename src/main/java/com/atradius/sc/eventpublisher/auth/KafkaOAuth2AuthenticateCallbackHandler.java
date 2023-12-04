package com.atradius.sc.eventpublisher.auth;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import reactor.core.publisher.Mono;

public class KafkaOAuth2AuthenticateCallbackHandler implements AuthenticateCallbackHandler {

  private static final Duration ACCESS_TOKEN_REQUEST_BLOCK_TIME = Duration.ofSeconds(30);
  private static final String TOKEN_AUDIENCE_FORMAT = "%s://%s/.default";

  private Function<TokenCredential, Mono<OAuthBearerTokenImp>> resolveToken;
  private final TokenCredential credential = new DefaultAzureCredentialBuilder().build();

  @Override
  public void configure(
      Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
    TokenRequestContext request = buildTokenRequestContext(configs);
    this.resolveToken =
        tokenCredential -> tokenCredential.getToken(request).map(OAuthBearerTokenImp::new);
  }

  private TokenRequestContext buildTokenRequestContext(Map<String, ?> configs) {
    URI uri = buildEventHubsServerUri(configs);
    String tokenAudience = buildTokenAudience(uri);

    TokenRequestContext request = new TokenRequestContext();
    request.addScopes(tokenAudience);
    return request;
  }

  @SuppressWarnings("unchecked")
  private URI buildEventHubsServerUri(Map<String, ?> configs) {
    String bootstrapServer = configs.get(BOOTSTRAP_SERVERS_CONFIG).toString();
    bootstrapServer = bootstrapServer.replaceAll("\\[|\\]", "");
    URI uri = URI.create("https://" + bootstrapServer);
    return uri;
  }

  private String buildTokenAudience(URI uri) {
    return String.format(TOKEN_AUDIENCE_FORMAT, uri.getScheme(), uri.getHost());
  }

  @Override
  public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof OAuthBearerTokenCallback oauthCallback) {
        this.resolveToken
            .apply(credential)
            .doOnNext(oauthCallback::token)
            .doOnError(
                throwable -> oauthCallback.error("invalid_grant", throwable.getMessage(), null))
            .block(ACCESS_TOKEN_REQUEST_BLOCK_TIME);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }

  @Override
  public void close() {
    // NOOP
  }
}
