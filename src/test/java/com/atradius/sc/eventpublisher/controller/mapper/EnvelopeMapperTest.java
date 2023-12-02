package com.atradius.sc.eventpublisher.controller.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.atradius.andromeda.event.framework.v1.EnvelopeV1;
import com.atradius.event_publisher.model.PublishEventRequest;
import com.atradius.sc.eventpublisher.controller.PublishEventRequestMother;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EnvelopeMapperTest {

  EnvelopeMapper sut = new EnvelopeMapper();

  @Test
  void testGivenCorrectObjectExpectCorrectMapping() {
    Map<String, Object> resources = Map.of("Key", 1);
    PublishEventRequest request =
        PublishEventRequestMother.createEventRequest().resources(resources);

    EnvelopeV1<Object> result = (EnvelopeV1<Object>) sut.map(request, resources);

    assertEquals(request.getEventName(), result.getEventName());
    assertEquals(UUID.fromString(request.getEventUuid()), result.getEventUuid());
    assertEquals(request.getEventDomain(), result.getEventDomain());
    assertEquals(request.getEventType(), result.getEventType());
    assertEquals(request.getEventSubtype(), result.getEventSubtype());
    assertEquals(request.getEventDatetime(), result.getEventDatetime());
    assertEquals(request.getEventOriginId(), result.getEventOriginId());
    assertEquals(request.getObjectType(), result.getObjectType());
    assertEquals(request.getObjectId(), result.getObjectId());
    assertEquals(request.getCorrelationId(), result.getCorrelationId());
    assertEquals(resources, result.getResources());
    assertEquals(request.getPayload(), result.getPayload());
  }
}
