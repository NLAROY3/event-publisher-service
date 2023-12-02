package com.atradius.sc.eventpublisher.config;

import com.atradius.andromeda.event.framework.Envelope;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

  private final ProducerFactory<String, Envelope<?>> kafkaProducerFactory;

  @Bean
  public KafkaTemplate<String, Envelope<?>> andromedaKafkaTemplate(
      final KafkaProperties properties) {
    final PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

    final KafkaTemplate<String, Envelope<?>> kafkaTemplate =
        new KafkaTemplate<>(kafkaProducerFactory);
    map.from(properties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
    map.from(properties.getTemplate().getTransactionIdPrefix())
        .to(kafkaTemplate::setTransactionIdPrefix);

    kafkaTemplate.setObservationEnabled(true);
    return kafkaTemplate;
  }
}
