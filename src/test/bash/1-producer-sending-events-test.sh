#!/bin/sh

# shellcheck disable=SC2164
## In the Azure pipeline we should change the directory to make it work relative paths
#cd src/test/bash

# shellcheck disable=SC2039
source ./event_publisher.sh
# shellcheck disable=SC2039
source ./authentication_service.sh

default_max_requests=200

# Sent a group of 1..N events using the REST API
main() {
  # shellcheck disable=SC2155
  local iterations=$(checkIterationParam "$1")
  # shellcheck disable=SC2155
  local env=$(checkEnvParam "$2")

  printf "Environment: %s \n" $env
  initAuthSvc "$env"
  initEPS "$env"
  initStressConsumer "$env"

  printGetTokenRequest

  token=$(getToken)

  for ((i = 0 ; i < $iterations ; i++)); do
    event_uuid=$(uuid)
    object_id=$((1 + $RANDOM % 10))
    correlation_id=$(uuid)

    message=$(getMessageFromTemplate "$event_uuid" $object_id, "$correlation_id")
    publishEvent "$i" "$message" "$event_uuid" $object_id "$correlation_id" "$token"
  done

  exit 0
}


main "$1"