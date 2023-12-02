package com.atradius.sc.eventpublisher.controller.mapper;

import com.atradius.andromeda.event.framework.Envelope;
import com.atradius.andromeda.event.framework.v1.EnvelopeV1;
import com.atradius.event_publisher.model.PublishEventRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Creater of the envelope from the event framework library */
@Component
public class EnvelopeMapper {
  /**
   * Converts from the Publish Event Request to a Envelope for publishing events.
   *
   * @param publishEventRequest the request that comes in from the controller.
   * @param resources the resources that must be a valid Map
   * @return return the Evenlope as created by the Envelope builder from the event framework library
   */
  public Envelope<Object> map(
      PublishEventRequest publishEventRequest, Map<String, Object> resources) {
    EnvelopeV1.EnvelopeV1Builder<Object> envelope =
        Envelope.builder(publishEventRequest.getPayload())
            .eventName(publishEventRequest.getEventName())
            .eventDomain(publishEventRequest.getEventDomain())
            .eventType(publishEventRequest.getEventType())
            .eventSubtype(publishEventRequest.getEventSubtype())
            .eventOriginId(publishEventRequest.getEventOriginId())
            .objectType(publishEventRequest.getObjectType())
            .objectId(publishEventRequest.getObjectId())
            .correlationId(publishEventRequest.getCorrelationId())
            .resources(resources);

    if (publishEventRequest.getEventUuid() != null) {
      envelope.eventUuid(UUID.fromString(publishEventRequest.getEventUuid()));
    }
    if (publishEventRequest.getEventDatetime() != null) {
      envelope.eventDatetime(publishEventRequest.getEventDatetime());
    }

    return envelope.build();
  }
}
