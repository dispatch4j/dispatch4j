# Dispatch4j 🚀

[![CI](https://github.com/dispatch4j/dispatch4j/workflows/CI/badge.svg)](https://github.com/dispatch4j/dispatch4j/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/technologies/downloads/)

Welcome to **Dispatch4j** - a simple, high-performance CQRS (Command Query Responsibility Segregation) 
command dispatcher library for Java 21+! Built with simplicity and performance in mind. 
.

## ✨ Why Dispatch4j?

We believe that implementing CQRS patterns shouldn't be complicated. Dispatch4j was born from the need for a lightweight, yet powerful solution that:

- **Just Works** - Minimal configuration with sensible defaults
- **Stays Out of Your Way** - Clean, annotation-based approach that feels natural
- **Scales With You** - From small projects to enterprise applications
- **Embraces Modern Java** - Leveraging Java 21+ features for cleaner, more efficient code

Whether you're building microservices, modular monoliths, or event-driven systems, Dispatch4j provides the foundation you need to separate your commands, queries, and events effectively.

## 🎯 Key Features

- **Simple, Intuitive API** - Get started in minutes with our annotation-based approach
- **Spring Boot Integration** - Seamless integration with automatic handler discovery
- **Standalone Mode** - Use without any framework dependencies
- **Async Support** - Built-in CompletableFuture support for non-blocking operations
- **Type Safety** - Compile-time checking with strong typing
- **Thread-Safe** - Production-ready concurrent execution
- **Minimal Dependencies** - Core module only requires SLF4J

## 🚀 Getting Started

### Maven
```xml
<dependency>
    <groupId>io.github.dispatch4j</groupId>
    <artifactId>dispatch4j-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```kotlin
implementation("io.github.dispatch4j:dispatch4j-spring-boot-starter:1.0.0")
```

### Quick Example

Define your command:
```java
@Command
public record CreateOrderCommand(String customerId, List<String> items) {}
```

Create a handler:
```java
@Component
public class OrderService {
    @CommandHandler
    public OrderCreatedEvent handle(CreateOrderCommand command) {
        // Your business logic here
        return new OrderCreatedEvent(orderId, command.customerId());
    }
}
```

Dispatch commands:
```java
@RestController
public class OrderController {
    private final Dispatcher dispatcher;

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest request) {
        var command = new CreateOrderCommand(request.customerId(), request.items());
        var event = dispatcher.send(command);
        return ResponseEntity.ok("Order created: " + event.orderId());
    }
}
```

That's it! Dispatch4j automatically discovers your handlers and wires everything together.

## 🛠️ Modules

Dispatch4j is modular by design, allowing you to use only what you need:

- **dispatch4j-core** - Core CQRS implementation (minimal dependencies)
- **dispatch4j-spring-boot-starter** - Spring Boot auto-configuration
- **dispatch4j-spring-boot-autoconfigure** - Spring integration components
- **dispatch4j-standalone** - Factory for standalone usage
- **dispatch4j-examples** - Sample applications and patterns

## 🤝 Contributing

We're thrilled that you're interested in contributing to Dispatch4j! 
This project thrives thanks to amazing contributors like you. Whether you're fixing bugs, adding features, 
improving documentation, or sharing ideas - every contribution matters and is genuinely appreciated.

### How You Can Help

- **Report Issues** - Found a bug or have a feature request? [Open an issue](https://github.com/dispatch4j/dispatch4j/issues)!
- **Submit Pull Requests** - Check our [open issues](https://github.com/dispatch4j/dispatch4j/issues) or implement your own ideas
- **Improve Documentation** - Help others learn by enhancing our docs
- **Share Your Experience** - Blog posts, talks, or social media - spread the word!
- **Answer Questions** - Help the community in discussions and issues

### Getting Started with Development

```bash
# Clone the repository
git clone https://github.com/dispatch4j/dispatch4j.git

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :dispatch4j-core:test
```

Check out our [CONTRIBUTING.md](CONTRIBUTING.md) guide for detailed information on our development process, coding standards, and how to submit pull requests.

## 📈 Roadmap

We're excited about the future of Dispatch4j! Here's what's coming:

- ✅ Core CQRS implementation
- ✅ Spring Boot integration
- ✅ Async operations support
- 🚧 Handlers middleware
- 🚧 Support for interfaces
- 🚧 Processing pipelines
- 📋 Additional framework integrations

Have ideas? We'd love to hear them! Open a discussion or issue to share your thoughts.

## 📄 License

Dispatch4j is open source software released under the [Apache 2.0 License](LICENSE). Use it freely in your personal and commercial projects!

## 🙏 Acknowledgments

A heartfelt thank you to:
- All our contributors who make this project possible
- The Java community for continuous inspiration
- Projects like Axon Framework and MediatR that paved the way
- You, for choosing Dispatch4j for your project!

---

**Ready to simplify your CQRS implementation?** Star ⭐ this repository and start building with Dispatch4j today!

*Built with ❤️ by the Dispatch4j community*