package io.github.dispatch4j.examples.ecommerce.controller;

import io.github.dispatch4j.Dispatcher;
import io.github.dispatch4j.examples.ecommerce.command.CreateOrderCommand;
import io.github.dispatch4j.examples.ecommerce.handler.OrderQueryHandler.OrderView;
import io.github.dispatch4j.examples.ecommerce.query.GetOrderQuery;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final Dispatcher dispatcher;

    public OrderController(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {
        var command =
                new CreateOrderCommand(
                        request.customerId(),
                        request.productId(),
                        request.quantity(),
                        request.price());

        var orderId = (String) dispatcher.send(command);

        return ResponseEntity.ok(new CreateOrderResponse(orderId));
    }

    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<CreateOrderResponse>> createOrderAsync(
            @RequestBody CreateOrderRequest request) {
        var command =
                new CreateOrderCommand(
                        request.customerId(),
                        request.productId(),
                        request.quantity(),
                        request.price());

        return dispatcher
                .sendAsync(command)
                .thenApply(orderId -> ResponseEntity.ok(new CreateOrderResponse((String) orderId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String orderId) {
        var query = new GetOrderQuery(orderId);
        var order = (OrderView) dispatcher.send(query);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/async")
    public CompletableFuture<ResponseEntity<OrderView>> getOrderAsync(
            @PathVariable String orderId) {
        var query = new GetOrderQuery(orderId);
        return dispatcher.sendAsync(query).thenApply(order -> ResponseEntity.ok((OrderView) order));
    }

    public record CreateOrderRequest(
            String customerId, String productId, int quantity, double price) {}

    public record CreateOrderResponse(String orderId) {}
}
