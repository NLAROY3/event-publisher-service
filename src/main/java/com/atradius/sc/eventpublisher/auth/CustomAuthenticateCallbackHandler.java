package com.atradius.sc.eventpublisher.auth;

import com.azure.core.credential.TokenRequestContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticateCallbackHandler implements AuthenticateCallbackHandler {
  // final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
  //  private AppServiceMSICredentials credentials;
  //  private final DefaultAzureCredential defaultCredential =
  //      new DefaultAzureCredentialBuilder()
  //          .workloadIdentityClientId("fa125719-a358-47de-8805-2242ef5716b4")
  //          .build();

  private final IdentityConfig defaultCredential;

  // private final MSICredentials credentials;
  private String sbUri;

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers0;

  //  @Value("${spring.kafka.bootstrap-servers}")
  //  public void setBootstrapServers0(String myValue) {
  //    this.bootstrapServers0 = myValue;
  //  }

  // @Value("${spring.kafka.bootstrap-servers}")
  private final String bootstrapServers = "sc-weu-sit-eventhub-ns.servicebus.windows.net";

  //  private AppServiceMSICredentials getCredentials() {
  //    if (isNull(credentials)) {
  //      credentials = new AppServiceMSICredentials(AzureEnvironment.AZURE);
  //    }
  //    return credentials;
  //  }

  //  private MSICredentials getCredentials() {
  //    if (isNull(credentials)) {
  //      credentials = new MSICredentials();
  //    }
  //    return credentials;
  //  }  private MSICredentials getCredentials() {
  //    if (isNull(credentials)) {
  //      credentials = new MSICredentials();
  //    }
  //    return credentials;
  //  }

  @Override
  public void configure(
      Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
    log.info("bootstrapServers0 {}", bootstrapServers0);
    log.info("bootstrapServers {}", bootstrapServers);
    // log.info("bootstrapServers {}", bootstrapServers.replaceAll("\\[|\\]", ""));
    // bootstrapServers = bootstrapServers.replaceAll("\\[|\\]", "");
    URI uri = URI.create("https://" + bootstrapServers);
    this.sbUri = uri.getScheme() + "://" + uri.getHost();
  }

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof OAuthBearerTokenCallback) {
        try {
          OAuthBearerToken token = getOAuthBearerToken();
          OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
          oauthCallback.token(token);
        } catch (InterruptedException | ExecutionException | TimeoutException | ParseException e) {
          e.printStackTrace();
        }
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }

  OAuthBearerToken getOAuthBearerToken()
      throws InterruptedException,
          ExecutionException,
          TimeoutException,
          IOException,
          ParseException {
    log.info("Get Oauth bearer token");
    log.info("bootstrapServers0: {}", bootstrapServers0);
    log.info("bootstrapServers: {}", bootstrapServers);

    String accessToken =
        defaultCredential.defaultAzureCredential()
            .getTokenSync(new TokenRequestContext().addScopes("https://" + bootstrapServers + "/.default"))
            .getToken();

    log.info("accessToken: {}", accessToken);
    // String accessToken = getCredentials().getToken(sbUri);
    JWT jwt = JWTParser.parse(accessToken);
    JWTClaimsSet claims = jwt.getJWTClaimsSet();
    OffsetDateTime expirationTime =
        defaultCredential.defaultAzureCredential()
            .getTokenSync(new TokenRequestContext().addScopes("https://" + bootstrapServers + "/.default"))
            .getExpiresAt()
            .toInstant()
            .atOffset(ZoneOffset.UTC);
    log.info("expirationTime: {}", expirationTime);
    Date expirationTime2 = Date.from(expirationTime.toInstant());
    log.info("expirationTime2: {}", expirationTime2);
    return new OAuthBearerTokenImpl(accessToken, expirationTime2); // claims.getExpirationTime());
  }

  public void close() throws KafkaException {}
}
