package io.github.dispatch4j.spring;

import static org.assertj.core.api.Assertions.*;

import io.github.dispatch4j.Dispatch4j;
import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.EventHandler;
import io.github.dispatch4j.handler.QueryHandler;
import io.github.dispatch4j.spring.config.Dispatch4jAutoConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@Disabled
@SpringBootTest(
        classes = {
            InterfaceBasedHandlerRegistrationTest.TestHandler.class,
            InterfaceBasedHandlerRegistrationTest.TestConfig.class,
            Dispatch4jAutoConfiguration.class
        })
class InterfaceBasedHandlerRegistrationTest {

    private static final Logger log =
            LoggerFactory.getLogger(InterfaceBasedHandlerRegistrationTest.class);

    @Autowired private Dispatch4j dispatcher;

    @Test
    void shouldRegisterInterfaceBasedCommandHandlers() {
        // Given
        var command = new SpringInterfaceCommand("spring-command-test");

        // When
        String result = dispatcher.send(command);

        // Then
        assertThat(result).isEqualTo("Spring interface command: spring-command-test");
    }

    @Test
    void shouldRegisterInterfaceBasedQueryHandlers() {
        // Given
        var query = new SpringInterfaceQuery("spring-query-test");

        // When
        String result = dispatcher.send(query);

        // Then
        assertThat(result).isEqualTo("Spring interface query: spring-query-test");
    }

    @Test
    void shouldRegisterInterfaceBasedEventHandlers() {
        // Given
        var event = new SpringInterfaceEvent("spring-event-test");

        // When & Then - Should not throw
        assertThatCode(() -> dispatcher.publish(event)).doesNotThrowAnyException();
    }

    // Test message types
    @Command
    public record SpringInterfaceCommand(String value) {}

    @Query
    public record SpringInterfaceQuery(String searchTerm) {}

    @Event
    public record SpringInterfaceEvent(String data) {}

    @Configuration
    static class TestConfig {

        @Bean
        public CommandHandler<SpringInterfaceCommand, String> springInterfaceCommandHandler() {
            return command -> "Spring interface command: " + command.value();
        }

        @Bean
        public QueryHandler<SpringInterfaceQuery, String> springInterfaceQueryHandler() {
            return query -> "Spring interface query: " + query.searchTerm();
        }

        @Bean
        public EventHandler<SpringInterfaceEvent> springInterfaceEventHandler() {
            return event -> log.info("Spring interface event handled: {}", event.data());
        }
    }

    @Component
    static class TestHandler implements CommandHandler<SpringInterfaceQuery, String> {
        @Override
        public String handle(SpringInterfaceQuery query) {
            return "Handled query: " + query.searchTerm();
        }
    }
}
