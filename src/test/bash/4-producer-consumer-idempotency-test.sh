#!/bin/sh

# shellcheck disable=SC2039
source ./event_publisher.sh
# shellcheck disable=SC2039
source ./authentication_service.sh
# shellcheck disable=SC2039
source ./event_consumer.sh
# shellcheck disable=SC2039
source ./common.sh

# Sent a group of N events using the REST API
main() {
  # shellcheck disable=SC2155
  local iterations=$(checkIterationParam "$1")
  # shellcheck disable=SC2155
  local env=$(checkEnvParam "$2")

  printf "Environment: %s \n" "$env"
  initAuthSvc "$env"
  initEPS "$env"
  initStressConsumer "$env"

  waiting_time_interval=2

  printGetTokenRequest
  token=$(getToken)

  # shellcheck disable=SC2004
  for ((i = 0 ; i < $iterations ; i++)); do
    event_uuid=$(uuid)
    object_id=$((1 + $RANDOM % 10))
    correlation_id=$(uuid)

    message=$(getMessageFromTemplate "$event_uuid" $object_id, "$correlation_id")
    printf "Publishing message with event_uuid %s once ...\n" "$event_uuid"
    publishEvent "$i" "$message" "$event_uuid" $object_id "$correlation_id" "$token"
    printf "Publishing same message with event_uuid %s twice ...\n" "$event_uuid"
    publishEvent "$i" "$message" "$event_uuid" $object_id "$correlation_id" "$token"

    printf "Waiting %s seconds ...\n" $waiting_time_interval
    sleep "${waiting_time_interval}"

    printf "Checking consumer ...\n"
    status=$(checkEventInConsumer "$i" "$event_uuid")
    echo "status:" "$status"

    if [[ "$status" != "200" ]]; then
        exit 1
    fi
  done

  exit 0
}


main "$1"