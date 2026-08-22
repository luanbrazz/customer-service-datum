package com.lb.notificationservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CustomerCreatedListener {

    @RabbitListener(queues = "customer.created.queue")
    public void handleCustomerCreated(Map<String, Object> event) {
        log.info(
                "Notificação: enviando e-mail de boas-vindas para {} ({})",
                event.get("name"),
                event.get("email")
        );
    }
}