package com.atradius.sc.eventpublisher.controller;

import com.atradius.event_publisher.model.PublishEventRequest;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class PublishEventRequestMother {
  private static final OffsetDateTime TEST_TIME = OffsetDateTime.now();

  public static PublishEventRequest createEventRequest() {
    return new PublishEventRequest()
        .eventName("My IGW event")
        .eventUuid(UUID.randomUUID().toString())
        .eventDomain("IGW Things")
        .eventType("IGW create test")
        .eventSubtype("CREATE")
        .eventDatetime(TEST_TIME)
        .eventOriginId("IGW")
        .objectType("CLD")
        .objectId("1")
        .correlationId(UUID.randomUUID().toString())
        .resources(Map.of("Key", 2))
        .payload("I'm a valid payload");
  }
}
