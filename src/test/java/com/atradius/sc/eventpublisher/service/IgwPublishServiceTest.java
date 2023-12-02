package com.atradius.sc.eventpublisher.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.atradius.andromeda.event.framework.Envelope;
import com.atradius.sc.eventpublisher.controller.PublishEventRequestMother;
import com.atradius.sc.eventpublisher.controller.mapper.EnvelopeMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("Igw Publish Service Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = IgwPublishService.class)
public class IgwPublishServiceTest {
  private final EnvelopeMapper envelopeMapper = new EnvelopeMapper();

  @MockBean private KafkaTemplate<String, Envelope<?>> kafkaTemplate;

  @Autowired private IgwPublishService igwPublishService;

  @Value("${atradius.igw.publish.topic-name}")
  private String topicName;

  @BeforeEach
  public void beforeEach() {
    Mockito.reset(kafkaTemplate);
  }

  @DisplayName(
      """
                 Given a event message
                 When The Kafka Producer sends the event
                 Then Producer receives an ACK""")
  @Test
  void shouldSendMessageOK() {
    // Given
    var publishEventRequest = PublishEventRequestMother.createEventRequest();
    var envelope = envelopeMapper.map(publishEventRequest, Map.of("key", "value"));
    var messageResponseFuture = createMessageResponse(envelope);
    when(kafkaTemplate.send(topicName, envelope)).thenReturn(messageResponseFuture);
    // When
    var result = Assertions.assertDoesNotThrow(() -> igwPublishService.send(envelope));
    // Then
    Assertions.assertEquals(envelope, result.getProducerRecord().value());
    Assertions.assertTrue(result.getRecordMetadata().hasOffset());
    Mockito.verify(kafkaTemplate, times(1)).send(eq(topicName), any(Envelope.class));
  }

  @DisplayName(
      """
                 Given a event message
                 When The Kafka Producer sends the event
                 Then Producer receives an exception""")
  @Test
  void shouldSendMessageKO() {
    // Given
    var envelope =
        envelopeMapper.map(PublishEventRequestMother.createEventRequest(), Map.of("key", "value"));
    when(kafkaTemplate.send(topicName, envelope)).thenThrow(ProducerFencedException.class);
    // When
    Assertions.assertThrows(ProducerFencedException.class, () -> igwPublishService.send(envelope));
    // Then
    Mockito.verify(kafkaTemplate, times(1)).send(eq(topicName), any(Envelope.class));
  }

  @DisplayName(
      """
     Given 1 event message
     When The First time fails but the second works
     Then Producer retries to send it successfully""")
  @Test
  void shouldSendMessageWithRetries() {
    // Given
    var publishEventRequest = PublishEventRequestMother.createEventRequest();
    var envelope = envelopeMapper.map(publishEventRequest, Map.of("key", "value"));
    var messageResponseFuture = createMessageResponse(envelope);

    when(kafkaTemplate.send(topicName, envelope))
        .thenThrow(AuthorizationException.class)
        .thenReturn(messageResponseFuture);
    // When
    // First call
    Assertions.assertThrows(AuthorizationException.class, () -> igwPublishService.send(envelope));
    // Second call
    var result = Assertions.assertDoesNotThrow(() -> igwPublishService.send(envelope));
    // This template is mocked. With a real template with configurations it should do this in one
    // step
    // Then
    Mockito.verify(kafkaTemplate, times(2)).send(eq(topicName), any(Envelope.class));
    Assertions.assertTrue(result.getRecordMetadata().hasOffset());
    Assertions.assertEquals(envelope, result.getProducerRecord().value());
  }

  @DisplayName(
      """
                 Given 2 event messages
                 When The First message fails one time
                 Then Producer retries to send it in the same order""")
  @Test
  void shouldSendMessageWithRetriesAndOrder() {
    // Given
    var publishEventRequest1 = PublishEventRequestMother.createEventRequest();
    var envelope1 = envelopeMapper.map(publishEventRequest1, Map.of("key", "value"));
    var messageResponse1Future = createMessageResponse(envelope1);

    var publishEventRequest2 = PublishEventRequestMother.createEventRequest();
    var envelope2 = envelopeMapper.map(publishEventRequest2, Map.of("key", "value"));
    var messageResponse2Future = createMessageResponse(envelope2);

    when(kafkaTemplate.send(topicName, envelope1))
        .thenThrow(AuthorizationException.class)
        .thenReturn(messageResponse1Future);

    when(kafkaTemplate.send(topicName, envelope2)).thenReturn(messageResponse2Future);
    // When
    // First call
    Assertions.assertThrows(AuthorizationException.class, () -> igwPublishService.send(envelope1));
    // Second call
    var result = Assertions.assertDoesNotThrow(() -> igwPublishService.send(envelope2));
    // Then
    Assertions.assertTrue(result.getRecordMetadata().hasOffset());
    Assertions.assertEquals(envelope2, result.getProducerRecord().value());
    Mockito.verify(kafkaTemplate, times(2)).send(eq(topicName), any(Envelope.class));
  }

  private CompletableFuture<SendResult<String, Envelope<?>>> createMessageResponse(
      Envelope<Object> envelope) {
    var producerRecord = new ProducerRecord<String, Envelope<?>>(topicName, envelope);
    var recordMetadata = new RecordMetadata(new TopicPartition(topicName, 0), 1L, 0, 0, 1024, 1024);
    return CompletableFuture.supplyAsync(() -> new SendResult<>(producerRecord, recordMetadata));
  }
}
