package com.atradius.sc.eventpublisher.controller;

import com.atradius.event_publisher.api.IgwPublishApi;
import com.atradius.event_publisher.model.PublishEventRequest;
import com.atradius.sc.eventpublisher.controller.mapper.EnvelopeMapper;
import com.atradius.sc.eventpublisher.service.IgwPublishService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The IGW Public API Controller */
@Slf4j
@RestController
@RequiredArgsConstructor
public class IgwPublishApiController implements IgwPublishApi {

  private final IgwPublishService igwPublishService;

  private final EnvelopeMapper envelopeMapper;

  @Override
  // java:S2142 is regarding "ignoring" the interrupt exceptions, we aren't we are throwing a new
  // error to inform the user of the issue.
  @SuppressWarnings("java:S2142")
  public ResponseEntity<Void> publishEvent(@Valid PublishEventRequest publishEventRequest) {
    Map<String, Object> resources = verifyResources(publishEventRequest);

    try {
      log.info("Publishing message with event UUID {}", publishEventRequest.getEventUuid());
      igwPublishService.send(envelopeMapper.map(publishEventRequest, resources));
    } catch (ExecutionException | InterruptedException e) {
      log.warn("Publishing to Event Hub went wrong, because of the following issues", e);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Publishing to Event Hub went wrong, because of the following issues:",
          e);
    } catch (TimeoutException e) {
      log.warn("Publishing to Event Hub took too long", e);
      throw new ResponseStatusException(
          HttpStatus.GATEWAY_TIMEOUT, "Publishing to Event Hub took too long");
    }

    return ResponseEntity.status(HttpStatus.CREATED.value()).build();
  }

  /**
   * This method verifies whether the resources on the publishEventRequest are of type
   * Map&lt;String, Object&gt;. When test this to cast to a map and then to verify that every key in
   * the map is of the Type String, or a child of String. The reason for testing the keys
   * individually, is that in testing the code I noticed that a Map&lt;Object, Object&gt; would
   * properly pass this function.
   *
   * @param publishEventRequest The request to test.
   * @return The casted map, which has been sucessfully verified.
   * @throws ResponseStatusException When PublishEventRequest.getResources() isn't compatible with
   *     Map&lt;String, Object&gt;
   */
  @SuppressWarnings({"java:S1166", "unchecked"})
  private static Map<String, Object> verifyResources(PublishEventRequest publishEventRequest) {
    try {
      Map<String, Object> resources = (Map<String, Object>) publishEventRequest.getResources();
      Set<?> strings = resources.keySet();
      if (!strings.stream().allMatch(obj -> String.class.isAssignableFrom(obj.getClass()))) {
        throw new AssertionError("One or more keys is not a assignable to the String class.");
      }
      return resources;
    } catch (ClassCastException | AssertionError e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Resources doesn't represent a map of String-Object pairs.");
    }
  }
}
