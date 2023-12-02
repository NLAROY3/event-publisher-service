package com.atradius.sc.eventpublisher.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.atradius.event_publisher.model.PublishEventRequest;
import com.atradius.sc.eventpublisher.controller.mapper.EnvelopeMapper;
import com.atradius.sc.eventpublisher.service.IgwPublishService;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class IgwPublishApiControllerTest {

  private final IgwPublishService igwPublishService = mock(IgwPublishService.class);
  private final IgwPublishApiController sut =
      new IgwPublishApiController(igwPublishService, new EnvelopeMapper());

  @Test
  void testGivenStringResourcesExpectResponseStatusException() {
    PublishEventRequest request =
        PublishEventRequestMother.createEventRequest().resources("not a map");

    ResponseStatusException responseStatusException =
        assertThrows(ResponseStatusException.class, () -> sut.publishEvent(request));
    assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatusCode());
  }

  @Test
  void testGivenWrongMapResourcesExpectResponseStatusException() {
    PublishEventRequest request =
        PublishEventRequestMother.createEventRequest()
            .resources(Map.of(new Object(), new Object()));

    ResponseStatusException responseStatusException =
        assertThrows(ResponseStatusException.class, () -> sut.publishEvent(request));
    assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatusCode());
  }

  @Test
  void testGivenRightMapResourceExpectNoException() {
    PublishEventRequest request = PublishEventRequestMother.createEventRequest();

    Assertions.assertDoesNotThrow(() -> sut.publishEvent(request));
  }
}
