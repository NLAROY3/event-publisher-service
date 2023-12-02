# Introduction

In this folder we have defined some bash scripts to check the connectivity of the event-framework-service 
with the Azure EventHub (or Kafka cluster) and perform a set of basic operations to check the environment quickly.

## Test scripts

### Execute script 1
By default, the script sends 1 request by default to the REST API with random values in the body, generated from a json template.

```bash
bash 1-producer-sending-events-test.sh 
```
or
```bash
./1-producer-sending-events-test.sh 
```

Optional arguments:
#num_iterations: You can specify the number of requests invoking the script with a parameter. 
For instance, if you want to send 10 requests instead of the 1 we have by default you can execute this command:
#environment (default:sit)

```bash
bash 1-producer-sending-events-test.sh 10 dev
```
or
```bash
./1-producer-sending-events-test.sh 10 dev
```

### Execute script 2

This script will wait the max idle connection time (connections.max.idle.ms) using intervals of 30 secs.
nAfter that, it will send an event to check if the connectivity is ok.

```bash
bash 2-max-idle-connections-test.sh
```
or
```bash
./2-max-idle-connections-test.sh
```

Optional arguments:
#environment (default:sit)

```bash
bash 2-max-idle-connections-test.sh dev
```
or
```bash
./2-max-idle-connections-test.sh dev
```

### Execute script 3

This script will send an event to the producer and after that will call to an endpoint in the [aks-eventhub-stress-tests-consumer](https://github.com/AtradiusGroup/aks-eventhub-stress-tests-consumer) to verify the event using the eventId.

```bash
bash 3-producer-consumer-path-test.sh
```
or
```bash
./3-producer-consumer-path-test.sh
```

Optional arguments:
#num_iterations (default: 1)
#environment (default:sit)

### Execute script 4

This script will send twice an event to the producer and after that will call to an endpoint in the [aks-eventhub-stress-tests-consumer](https://github.com/AtradiusGroup/aks-eventhub-stress-tests-consumer) to verify the event using the eventId.

```bash
bash 4-producer-consumer-idempotency-test.sh
```
or
```bash
./4-producer-consumer-idempotency-test.sh
```

Optional arguments:
#num_iterations (default: 1)
#environment (default:sit)
