package io.github.dispatch4j.examples.ecommerce.query;

import io.github.dispatch4j.core.annotation.Query;

@Query
public record GetOrderQuery(String orderId) {}
