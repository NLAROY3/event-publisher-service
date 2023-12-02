#!/bin/sh

default_max_requests=200

##
# Method to generate random UUIDs
# On linux we could use $uuid method, but this is not available from a Windows environment
#
uuid()
{
    # shellcheck disable=SC2039
    local N B C='89ab'

    # shellcheck disable=SC2039
    for (( N=0; N < 16; ++N ))
    do
        # shellcheck disable=SC2004
        B=$(( $RANDOM%256 ))

        case $N in
            6)
                printf '4%x' $(( B%16 ))
                ;;
            8)
                printf '%c%x' ${C:$RANDOM%${#C}:1} $(( B%16 ))
                ;;
            3 | 5 | 7 | 9)
                printf '%02x-' $B
                ;;
            *)
                printf '%02x' $B
                ;;
        esac
    done

    echo
}

# This method has 1 input parameter with iteration
checkIterationParam() {
    # shellcheck disable=SC2039
    if [[ -n $1 ]] && [ "$1" -ge 0 ] && [ "$1" -le $default_max_requests ]; then
      echo "$1"
    else
      #Default iterations value
      echo 1
    fi
}

checkEnvParam() {
  # shellcheck disable=SC2039
  if [[ -n $1 ]]; then
    echo "$1"
  else
    # Default environment value
    echo "sit"
  fi
}