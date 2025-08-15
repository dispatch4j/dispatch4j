package io.github.dispatch4j.examples.ecommerce.event;

import io.github.dispatch4j.annotation.Event;
import java.time.Instant;

@Event
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        double price,
        Instant createdAt) {}
