package com.atradius.sc.eventpublisher.auth;

import static java.util.Objects.nonNull;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.aad.msal4j.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FederatedCredential implements TokenCredential {

  @Value("${AZURE_FEDERATED_TOKEN_FILE}")
  private String AZURE_FEDERATED_TOKEN_FILE = "/var/run/secrets/azure/tokens/azure-identity-token";

  @Value("${AZURE_AUTHORITY_HOST}")
  private String AZURE_AUTHORITY_HOST = "https://login.microsoftonline.com/";

  @Value("${AZURE_TENANT_ID}")
  private String AZURE_TENANT_ID = "e1b7a2d9-eada-49b6-9f5f-c9c63fd0e7b7";

  @Value("${AZURE_CLIENT_ID}")
  private String AZURE_CLIENT_ID = "fa125719-a358-47de-8805-2242ef5716b4";

  @Override
  public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {

    log.info("AZURE_FEDERATED_TOKEN_FILE: {} ", AZURE_FEDERATED_TOKEN_FILE);
    String clientAssertion = null;
    try {
      clientAssertion = Files.readString(Paths.get(AZURE_FEDERATED_TOKEN_FILE));
    } catch (IOException e) {
      log.error("Error getting AZURE_FEDERATED_TOKEN_FILE", e);
    }

    IClientCredential credential =
        ClientCredentialFactory.createFromClientAssertion(clientAssertion);

    log.info("AZURE_AUTHORITY_HOST: {} ", AZURE_AUTHORITY_HOST);
    log.info("AZURE_TENANT_ID: {} ", AZURE_TENANT_ID);
    log.info("AZURE_CLIENT_ID: {} ", AZURE_CLIENT_ID);

    StringBuilder authority = new StringBuilder();
    authority.append(AZURE_AUTHORITY_HOST);
    authority.append(AZURE_TENANT_ID);

    try {
      ConfidentialClientApplication app =
          ConfidentialClientApplication.builder(AZURE_CLIENT_ID, credential)
              .authority(authority.toString())
              .build();

      log.info("app: {} ", app);
      Set<String> scopes = new HashSet<>(tokenRequestContext.getScopes());
      log.info("scopes: {} ", scopes);

      ClientCredentialParameters parameters = ClientCredentialParameters.builder(scopes).build();

      log.info("parameters: {} ", parameters);

      IAuthenticationResult result = app.acquireToken(parameters).join();
      log.info("result: {} ", result);

      if (result != null) {
        log.info("account: {} ", result.account());
        log.info("idToken: {} ", result.idToken());
        log.info("tenantProfile: {} ", result.tenantProfile());
      }
      var token = nonNull(result) && nonNull(result.accessToken()) ? result.accessToken() : "";
      var expirationDate =
          nonNull(result) && nonNull(result.expiresOnDate())
              ? result.expiresOnDate().toInstant().atOffset(ZoneOffset.UTC)
              : null;

      log.info("token: {} ", token);
      log.info("expirationDate: {} ", expirationDate);

      AccessToken accessToken = new AccessToken(token, expirationDate);
      log.info("token: {} ", accessToken);

      return Mono.justOrEmpty(accessToken);
    } catch (Exception e) {
      log.error("Error creating client application.", e);
    }

    return Mono.empty();
  }
}
