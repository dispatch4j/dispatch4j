package io.github.dispatch4j.examples.ecommerce.handler;

import io.github.dispatch4j.core.annotation.EventHandler;
import io.github.dispatch4j.examples.ecommerce.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler {

  private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

  @EventHandler
  public void handle(OrderCreatedEvent event) {
    log.info(
        "Order created event received: orderId={}, customerId={}",
        event.orderId(),
        event.customerId());

    // Send confirmation email
    sendConfirmationEmail(event);

    // Update inventory
    updateInventory(event);
  }

  private void sendConfirmationEmail(OrderCreatedEvent event) {
    log.info("Sending confirmation email for order: {}", event.orderId());
    // Email sending logic here
  }

  private void updateInventory(OrderCreatedEvent event) {
    log.info(
        "Updating inventory for product: {}, quantity: {}", event.productId(), event.quantity());
    // Inventory update logic here
  }
}
