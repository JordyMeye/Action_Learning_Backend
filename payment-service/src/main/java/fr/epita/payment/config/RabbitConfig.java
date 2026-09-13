package fr.epita.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology (declared automatically on first connection):
 *
 *   payment.commands (direct) --payment.process--> [payment.process] --> PaymentCommandListener
 *                                                        |
 *                                  rejected after retries (dead-lettered)
 *                                                        v
 *   payment.commands.dlx (direct) --payment.process--> [payment.process.dlq] --> PaymentCommandListener#onDeadLetter
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "payment.commands";
    public static final String PROCESS_QUEUE = "payment.process";
    public static final String PROCESS_ROUTING_KEY = "payment.process";

    public static final String DEAD_LETTER_EXCHANGE = "payment.commands.dlx";
    public static final String DEAD_LETTER_QUEUE = "payment.process.dlq";

    @Bean
    public DirectExchange paymentCommandsExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange paymentDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    /** Durable: survives a broker restart. Rejected messages go to the dead-letter exchange. */
    @Bean
    public Queue processPaymentQueue() {
        return QueueBuilder.durable(PROCESS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(PROCESS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue processPaymentDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding processPaymentBinding() {
        return BindingBuilder.bind(processPaymentQueue()).to(paymentCommandsExchange()).with(PROCESS_ROUTING_KEY);
    }

    @Bean
    public Binding processPaymentDeadLetterBinding() {
        return BindingBuilder.bind(processPaymentDeadLetterQueue()).to(paymentDeadLetterExchange()).with(PROCESS_ROUTING_KEY);
    }

    /** Message bodies are JSON; listeners get them converted to their parameter type. */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
