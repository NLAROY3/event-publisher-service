package com.atradius.sc.eventpublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/** The app */
@SpringBootApplication
@EnableKafka
public class EventPublisherApp {

  /**
   * Main method.
   *
   * @param args The arguments.
   */
  public static void main(final String[] args) {

    SpringApplication.run(EventPublisherApp.class, args);
  }
}
