package io.github.dispatch4j.examples.ecommerce.command;

import io.github.dispatch4j.core.annotation.Command;

@Command
public record CreateOrderCommand(String customerId, String productId, int quantity, double price) {}
