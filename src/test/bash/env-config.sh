#!/bin/sh

# This method has 3 input parameters event_uuid, object_id, correlation_id
getEnvDomainConfig(){
  # shellcheck disable=SC2039
  local env=$1;

  case $env in
      local)
        domain=localhost:8081
        api_base_url=http://$domain
      ;;
      dev)
        domain=apps.dev.sc.atradiusnet.com
        api_base_url=https://$domain
      ;;
      sit)
        domain=apps.sit.sc.atradiusnet.com
        api_base_url=https://$domain
      ;;
      *)
          # shellcheck disable=SC2039
          echo -e "An unexpected error has occurred."
          exit
      ;;
  esac

}

getAuthSvcConfig(){
  local env=$1;

  case $env in
      local|dev|sit)
        api_base_url=https://login-dev.atradiusnet.com
        oidm_domain_name=DEV_JET_WebGateDomain
        oauth_auth_token=R0xTU0RFMTphb2dvYWZzaXdpMjlydHE=
        oauth_scope=DEV_TokenPOC_RS.sharedcomponents
      ;;
      prd)
        api_base_url=https://login-prd.atradiusnet.com:443
        oidm_domain_name=PROD_JET_WebGateDomain
        oauth_auth_token=UFJPRF9TUFJfT0lEQ1dlYkdhdGVJRDpKR2tzbGJsdzkzNTUzZ3NnLWF0
        oauth_scope=PROD_SC_OIDCWebGateRS.SCPrd
      ;;
      *)
          echo -e "An unexpected error has occurred."
          exit
      ;;
  esac
}