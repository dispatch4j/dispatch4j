package io.github.dispatch4j.examples.ecommerce.handler;

import io.github.dispatch4j.core.Dispatcher;
import io.github.dispatch4j.core.annotation.CommandHandler;
import io.github.dispatch4j.examples.ecommerce.command.CreateOrderCommand;
import io.github.dispatch4j.examples.ecommerce.event.OrderCreatedEvent;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandHandler.class);

    private final Dispatcher dispatcher;

    public OrderCommandHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @CommandHandler
    public String handle(CreateOrderCommand command) {
        log.info(
                "Creating order for customer: {}, product: {}",
                command.customerId(),
                command.productId());

        var orderId = UUID.randomUUID().toString();

        // Simulate order creation logic

        // Publish domain event
        var event =
                new OrderCreatedEvent(
                        orderId,
                        command.customerId(),
                        command.productId(),
                        command.quantity(),
                        command.price(),
                        Instant.now());

        dispatcher.publish(event);

        return orderId;
    }
}
