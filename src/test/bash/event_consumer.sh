#!/bin/sh

check_event_path=/sc/aks-eventhub-stress-tests-consumer/events/{eventId}/verify

# This method expects a param with the environment name
initStressConsumer(){
  # shellcheck disable=SC2039
  local env=$1;
  getEnvDomainConfig "$env"
  # shellcheck disable=SC2154
  check_event_endpoint=${api_base_url}${check_event_path}
}

##
# Replace the current eventId path param in the url
#
getConsumerEndpointUrl(){
  var1='{eventId}'
  eventId=$1;

  endpoint=$(printf '%s\n' "$check_event_endpoint" | sed -e "s/${var1}/${eventId}/g");
  echo "$endpoint"
}

##
# This method receives a param with the eventId
##
checkConsumerEvent() {
  eventId=$1;

  endpoint=$(getConsumerEndpointUrl "$eventId")
  status=$(curl -k -s -o /dev/null -w "%{http_code}" \
                  -X POST -H "Content-Type: application/json" \
                  "${endpoint}")
  echo "$status"
}

# This method sends an event printing all the logs to be showed in the terminal
checkEventInConsumer() {
  i=$1
  eventId=$2

  endpoint=$(getConsumerEndpointUrl "$eventId")

  # shellcheck disable=SC2059
  printf "Sending message %s ... \n" "$i"
  printf "[POST] %s\n" "${endpoint}"
  printf "EventId: %s\n" "${eventId}"

  response=$(checkConsumerEvent "$eventId")
  printf "\n"
  printf "===> Status: %s" "${response}"
  printf "\n-------------------------------------------------------------\n\n"

  # shellcheck disable=SC2039
  if [[ "$response" != "200" ]]; then
        exit 1
  fi
}