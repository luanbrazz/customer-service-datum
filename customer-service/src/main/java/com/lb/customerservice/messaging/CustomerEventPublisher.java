package com.lb.customerservice.messaging;

import com.lb.customerservice.config.RabbitMQConfig;
import com.lb.customerservice.event.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CustomerEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCustomerCreated(CustomerCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        log.info("Evento customer.created publicado para o cliente id={}", event.customerId());
    }
}