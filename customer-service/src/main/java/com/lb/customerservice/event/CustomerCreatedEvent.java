package com.lb.customerservice.event;

public record CustomerCreatedEvent(Long customerId, String name, String email) {
}