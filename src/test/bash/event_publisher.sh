#!/bin/sh

# shellcheck disable=SC2039
source ./common.sh
source ./env-config.sh

publish_event_path=/sc/platforms/events/v1/publish-igw-event

# This method expects a param with the environment name
initEPS(){
  local env=$1;
  getEnvDomainConfig "$env"
  # shellcheck disable=SC2154
  publish_event_endpoint=${api_base_url}${publish_event_path}
}

# This method has 3 input parameters event_uuid, object_id, correlation_id
getMessageFromTemplate(){
  # shellcheck disable=SC2016
  var1='{{$randomUUID}}'
  # shellcheck disable=SC2016
  var2='{{$randomInt}}'
  # shellcheck disable=SC2016
  var3='{{$randomUUIDA}}'

  event_uuid=$1;
  object_id=$2;
  correlation_id=$3;

  message=$(sed "s/${var1}/${event_uuid}/g; s/${var2}/${object_id}/g;
  s/${var3}/${correlation_id}/g; s/\\\"/\"/g" < request-body-template.json )

  echo "$message"
}

# This method Sent a event invoking the REST API using a POST call
sendEvent() {
  # shellcheck disable=SC2039
  local body=$1
  local token=$2

  status=$(curl -k -s -o /dev/null -w "%{http_code}" -X POST \
                  -H "Content-Type: application/json" \
                  -H "Authorization: Bearer ${token}" \
                  --data "${body}" \
                  "${publish_event_endpoint}")
  echo "$status"
}

# This method sends an event printing all the logs to be showed in the terminal
publishEvent() {
  i=$1
  message=$2
  event_uuid=$3
  object_id=$4
  correlation_id=$5
  token=$6

  # shellcheck disable=SC2059
  printf "Sending message $i ... \n"

  # shellcheck disable=SC2059
  printf "[POST] ${publish_event_endpoint}\n";

  printf "EventId: %s\n" "${event_uuid}"
  printf "ObjectId: %s\n" "${object_id}"
  printf "CorrelationId: %s\n" "${correlation_id}"
  printf "Token: %s\n" "${token}"

  response=$(sendEvent "$message" "$token")
  printf "\n"
  printf "===> Status: %s" "${response}"
  printf "\n-------------------------------------------------------------\n\n"

  if [[ "$response" != "201" ]]; then
      exit 1
  fi
}
