FROM sc/common-docker-image:1.0.0
LABEL maintainer="Shared Components Platform <sc-platform@atradius.com>"

WORKDIR /app

COPY ./src/main/resources/cacerts /jre/lib/security/cacerts
COPY build/libs/event-publisher-*.jar app.jar

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=docker,azure", \
"-jar", "app.jar"]