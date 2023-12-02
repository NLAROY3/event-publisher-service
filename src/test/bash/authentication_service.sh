#!/bin/sh

oauth_token_path='/oauth2/rest/token'

initAuthSvc(){
  local env=$1;
  getAuthSvcConfig "$env"
  # shellcheck disable=SC2154
  oauth_token_endpoint=${api_base_url}${oauth_token_path}
}

# This method call to the OAuth API invoking the REST API using a POST call
# and retrieves the response
getToken() {
  # shellcheck disable=SC2154
  response=$(curl -k -s -o  -w "%{http_code}" -X POST \
                  -H "Content-Type: application/x-www-form-urlencoded" \
                  -H "Authorization: Basic ${oauth_auth_token}" \
                  --data-urlencode "grant_type=CLIENT_CREDENTIALS" \
                  --data-urlencode "scope=${oauth_scope}" \
                  "${oauth_token_endpoint}?iddomain=${oidm_domain_name}")

  token=$(echo "${response}" | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')

  echo "$token"
}

# This method sends an event printing all the logs to be showed in the terminal
printGetTokenRequest() {
  # shellcheck disable=SC2059
  printf "[POST] ${oauth_token_endpoint}\n";

  #response=$(getToken)
  #printf "\n"
  #printf "===> Response: %s" "${response}"
  #printf "\n-------------------------------------------------------------\n\n"
}
