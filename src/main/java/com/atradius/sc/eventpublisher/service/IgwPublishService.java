package com.atradius.sc.eventpublisher.service;

import com.atradius.andromeda.event.framework.Envelope;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

/** IGW Publish service, to send events to Azure event hub using the kafka template. */
@Service
@Slf4j
@RequiredArgsConstructor
public class IgwPublishService {

  public static final int TIMEOUT_IN_SECONDS = 30;

  @Value("${atradius.igw.publish.topic-name}")
  private String topicName;

  private final KafkaTemplate<String, Envelope<?>> kafkaTemplate;

  /**
   * The send method to call on the service to send the event.
   *
   * @param envelope The preconstructed envelope to send.
   * @return Send response containing the key and the envelope.
   * @throws ExecutionException When the execution failed.
   * @throws InterruptedException When the thread waiting was interrupted
   * @throws TimeoutException When it had to wait longer then the Timeout specified.
   */
  // java:S1452 is regarding the wildcard, which is related to the type of template we have above,
  // and thus irrelevant.
  // java:S1160 is about throwing more than 1 exception from a public method. We must distinguish
  // the different errors, so politely disagree.
  @SuppressWarnings({"java:S1452", "java:S1160"})
  public SendResult<String, Envelope<?>> send(Envelope<Object> envelope)
      throws ExecutionException, InterruptedException, TimeoutException {
    try {
      return kafkaTemplate.send(topicName, envelope).get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    } catch (ExecutionException | InterruptedException e) {
      // TODO: Convert technical exceptions into business exceptions
      log.error("{} sending the message. Exception: {}", e.getClass().getName(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Exception {} succeeded sending the message. Exception: {}",
          e.getClass().getName(),
          e.getMessage());
      throw e;
    }
  }
}
