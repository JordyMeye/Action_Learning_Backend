package fr.epita.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    /**
     * Created on startup if missing. 3 partitions let up to 3 instances of a consumer
     * group read in parallel; events with the same key (payment id) always share a
     * partition, so they are read in the order they were written.
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC).partitions(3).replicas(1).build();
    }
}
