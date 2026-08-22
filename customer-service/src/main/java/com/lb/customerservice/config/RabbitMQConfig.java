package com.lb.customerservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "customer.exchange";
    public static final String QUEUE = "customer.created.queue";
    public static final String ROUTING_KEY = "customer.created";

    @Bean
    public DirectExchange customerExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue customerCreatedQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding(Queue customerCreatedQueue, DirectExchange customerExchange) {
        return BindingBuilder.bind(customerCreatedQueue).to(customerExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}