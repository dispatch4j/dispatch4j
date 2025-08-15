package io.github.dispatch4j.examples.ecommerce.handler;

import io.github.dispatch4j.annotation.QueryHandler;
import io.github.dispatch4j.examples.ecommerce.query.GetOrderQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryHandler.class);

    @QueryHandler
    public OrderView handle(GetOrderQuery query) {
        log.info("Fetching order: {}", query.orderId());

        // Simulate database lookup
        return new OrderView(query.orderId(), "customer-123", "product-456", 2, 99.99, "CONFIRMED");
    }

    public record OrderView(
            String orderId,
            String customerId,
            String productId,
            int quantity,
            double price,
            String status) {}
}
