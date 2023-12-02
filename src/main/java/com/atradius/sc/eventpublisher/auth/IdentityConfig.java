package com.atradius.sc.eventpublisher.auth;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class IdentityConfig {

  @Value("${AZURE_CLIENT_ID}")
  private String clientId;

  @Value("${AZURE_TENANT_ID}")
  private String tenantId;

  @Value("${AZURE_AUTHORITY_HOST}")
  private String authorityHost;

  @Value("${AZURE_FEDERATED_TOKEN_FILE}")
  private String tokenFile;

  @Bean
  public TokenCredential defaultAzureCredential() {
    return new DefaultAzureCredentialBuilder().build();
  }
}
