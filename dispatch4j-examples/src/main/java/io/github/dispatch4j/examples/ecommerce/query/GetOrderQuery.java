package io.github.dispatch4j.examples.ecommerce.query;

import io.github.dispatch4j.annotation.Query;

@Query
public record GetOrderQuery(String orderId) {}
