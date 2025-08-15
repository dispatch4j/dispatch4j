package io.github.dispatch4j.middleware;

import static io.github.dispatch4j.middleware.MiddlewareContext.MessageType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiddlewareContextTest {

    private static final String KEY = "testKey";
    private static final String VALUE = "testValue";

    private MiddlewareContext context;

    @BeforeEach
    void setUp() {
        context = new MiddlewareContext(COMMAND, String.class);
    }

    @Test
    void shouldCreateContextWithMessageTypeAndClass() {
        // When & Then
        assertThat(context.getMessageType()).isEqualTo(COMMAND);
        assertThat(context.getMessageClass()).isEqualTo(String.class);
    }

    @Test
    void shouldSetAndGetAttributes() {
        // Given
        // When
        context.setAttribute(KEY, VALUE);
        var result = context.getAttribute(KEY);

        // Then
        assertThat(result).isPresent().hasValue(VALUE);
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentAttribute() {
        // When
        var result = context.getAttribute("nonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAttributeWithType() {
        // Given
        context.setAttribute(KEY, VALUE);

        // When
        var result = context.getAttribute(KEY, String.class);

        // Then
        assertThat(result).isPresent().hasValue(VALUE);
    }

    @Test
    void shouldReturnEmptyOptionalForWrongType() {
        // Given
        context.setAttribute(KEY, VALUE);

        // When
        Optional<Integer> result = context.getAttribute(KEY, Integer.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRemoveAttribute() {
        // Given
        context.setAttribute(KEY, VALUE);

        // When
        Object removed = context.removeAttribute(KEY);
        Optional<Object> result = context.getAttribute(KEY);

        // Then
        assertThat(removed).isEqualTo(VALUE);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnNullWhenRemovingNonExistentAttribute() {
        // When
        Object removed = context.removeAttribute("nonExistent");

        // Then
        assertThat(removed).isNull();
    }

    @Test
    void shouldCheckIfAttributeExists() {
        // Given
        context.setAttribute(KEY, VALUE);

        // When & Then
        assertThat(context.hasAttribute(KEY)).isTrue();
        assertThat(context.hasAttribute("nonExistent")).isFalse();
    }

    @Test
    void shouldGetAllAttributes() {
        // Given
        context.setAttribute("key1", "value1");
        context.setAttribute("key2", "value2");

        // When
        var attributes = context.getAttributes();

        // Then
        assertThat(attributes)
                .hasSize(2)
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2");
    }

    @Test
    void shouldReturnImmutableMapOfAttributes() {
        // Given
        context.setAttribute("key1", "value1");
        var attributes = context.getAttributes();

        // When & Then
        assertThatThrownBy(() -> attributes.put("key2", "value2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldSupportMethodChaining() {
        // When
        var result = context.setAttribute(KEY, VALUE);

        // Then
        assertThat(result).isSameAs(context);
    }
}
