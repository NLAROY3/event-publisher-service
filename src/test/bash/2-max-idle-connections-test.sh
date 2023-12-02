#!/bin/sh

# shellcheck disable=SC2039
source ./event_publisher.sh
# shellcheck disable=SC2039
source ./authentication_service.sh

# shellcheck disable=SC2034
waiting_time_interval=30
# shellcheck disable=SC2034
intervals_5min=10
intervals_10min=20
intervals_15min=30
intervals_20min=40

wait_intervals(){
  # shellcheck disable=SC2039
  local max_intevals=$1

  for ((i = 0 ; i < "$max_intevals" ; i++)); do
      printf "\nInterval (%s/%s)" "$i" "$max_intevals"
      printf "\nWaiting %s seconds ..." $waiting_time_interval
      sleep "${waiting_time_interval}"
  done
}

check_status_code(){
  local response=$1

  if [[ "$response" == 201 ]]; then
    printf "\nResult Case: OK.\n"
  else
    printf "\nResult Case: KO!!!!n"
    exit 1
  fi
}

check_wait_and_send(){
  local case_description=$1
  local intervals=$2

  # shellcheck disable=SC2059
  printf "$case_description"
  wait_intervals "$intervals"
  printf "\n"
  event_uuid=$(uuid)
  object_id=$((1 + $RANDOM % 10))
  correlation_id=$(uuid)
  message=$(getMessageFromTemplate "$event_uuid" $object_id, "$correlation_id")
  token=$(getToken)
  response=$(sendEvent "$message" "$token")
  check_status_code "$response"

  printf "\n-------------------------------------------------------------\n\n"
}

# Wait for the maximum idle time and try to send an event
main() {
  printf "In Azure Event Hub the Idle timeout is 4 minutes."
  printf "\nThis script will wait the max idle connection time (connections.max.idle.ms) using intervals of 30 secs."
  printf "\nAfter that, it will send an event to check if the connectivity is ok.\n"

  # shellcheck disable=SC2155
  local env=$(checkEnvParam "$1")

  printf "Environment: %s \n" "$env"
  initAuthSvc "$env"
  initEPS "$env"

  check_wait_and_send "\nCase A: 5 minutes.\n" $intervals_5min

  check_wait_and_send "\nCase B: 10 minutes.\n" $intervals_10min

  check_wait_and_send "\nCase C: 15 minutes.\n" $intervals_15min

  check_wait_and_send "\nCase D: 20 minutes.\n" $intervals_20min

  printf "Test finished."
}

main "$1"