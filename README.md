# Event-publisher-service

A simple API to publish events currently only for IGW, due to them being an external application and not being able to connect to Eventhub/kafka

## Description

The Event Publishing api's goal is to provide other applications, which can't access eventhub directly, the ability to push events onto eventhub.

The service would have an endpoint for each type of event. They should be highly limited as it's a far better decision to connect to eventhub directly rather then go though a Rest API, as for example error handling will become easier.

The API should manage sending the events directly and communicate back any errors that have occurred.

## Getting Started

### Dependencies

To run and build the application you need Java 17 installed.

### Installing

Run the following command to build the project and pull all the dependencies.
```bash
./gradlew clean build
```

### Executing program

To run it you need to add some environment variables to your run profile in the editor. This is to make the Azure profile active:
![Active profile](src/main/docs/images/override-active-profile.png)

And the following configuration properties to add the required info to connect to kafka:
![Config properties](src/main/docs/images/override-configuration-properties.png)

After setting these values, you should be able to start the application!

### Executing in the terminal

## Local

```bash
./gradlew bootRun --args='--spring.profiles.active=local --spring.config.location=./src/test/resources/'
```

### Docker Commands

```bash
docker build . -t event-publisher:0.5.0
```

```bash
docker run  --name event-publisher -d -p 8080:8080 event-publisher:0.5.0
```