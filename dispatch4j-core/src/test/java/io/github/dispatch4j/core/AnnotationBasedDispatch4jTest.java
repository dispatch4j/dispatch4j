package io.github.dispatch4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnnotationBasedDispatch4jTest {

  private Dispatch4j dispatcher;

  @Spy private MultiHandlerService multiHandlerService = new MultiHandlerService();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    dispatcher = new Dispatch4j();
  }

  @Test
  void shouldHandleMultipleCommandHandlersInSameClass() {
    // Given
    registerCommandHandler(CreateUserCommand.class, multiHandlerService::handleCreateUser);
    registerCommandHandler(UpdateUserCommand.class, multiHandlerService::handleUpdateUser);

    CreateUserCommand createCommand = new CreateUserCommand("john@example.com", "John Doe");
    UpdateUserCommand updateCommand =
        new UpdateUserCommand("user-123", "johnupdated@example.com", "John Updated");

    // When
    String userId = dispatcher.send(createCommand);
    String updateResult = dispatcher.send(updateCommand);

    // Then
    assertThat(userId).startsWith("user-");
    assertThat(updateResult).isEqualTo("updated-user-123");
    verify(multiHandlerService).handleCreateUser(createCommand);
    verify(multiHandlerService).handleUpdateUser(updateCommand);
  }

  @Test
  void shouldHandleMultipleQueryHandlersInSameClass() {
    // Given
    registerQueryHandler(GetUserQuery.class, multiHandlerService::handleGetUser);
    registerQueryHandler(SearchUsersQuery.class, multiHandlerService::handleSearchUsers);

    GetUserQuery getUserQuery = new GetUserQuery("user-123");
    SearchUsersQuery searchQuery = new SearchUsersQuery("john", 10);

    // When
    UserView user = dispatcher.send(getUserQuery);
    SearchResult result = dispatcher.send(searchQuery);

    // Then
    assertThat(user.id()).isEqualTo("user-123");
    assertThat(result.count()).isEqualTo(2);
    verify(multiHandlerService).handleGetUser(getUserQuery);
    verify(multiHandlerService).handleSearchUsers(searchQuery);
  }

  @Test
  void shouldHandleAllHandlerTypesInSameClass() {
    // Given - Register all handlers
    registerCommandHandler(CreateUserCommand.class, multiHandlerService::handleCreateUser);
    registerCommandHandler(UpdateUserCommand.class, multiHandlerService::handleUpdateUser);
    registerQueryHandler(GetUserQuery.class, multiHandlerService::handleGetUser);
    registerQueryHandler(SearchUsersQuery.class, multiHandlerService::handleSearchUsers);
    registerEventHandler(UserCreatedEvent.class, multiHandlerService::handleUserCreated);
    registerEventHandler(UserUpdatedEvent.class, multiHandlerService::handleUserUpdated);

    // When & Then - Commands
    String userId = dispatcher.send(new CreateUserCommand("test@example.com", "Test User"));
    String updateResult =
        dispatcher.send(new UpdateUserCommand("user-456", "updated@example.com", "Updated"));

    // When & Then - Queries
    UserView user = dispatcher.send(new GetUserQuery("user-789"));
    SearchResult search = dispatcher.send(new SearchUsersQuery("search", 5));

    // When & Then - Events
    dispatcher.publish(new UserCreatedEvent("user-123", "created@example.com", "Created User"));
    dispatcher.publish(new UserUpdatedEvent("user-456", "updated@example.com", "Updated User"));

    // Verify all interactions
    verify(multiHandlerService).handleCreateUser(any(CreateUserCommand.class));
    verify(multiHandlerService).handleUpdateUser(any(UpdateUserCommand.class));
    verify(multiHandlerService).handleGetUser(any(GetUserQuery.class));
    verify(multiHandlerService).handleSearchUsers(any(SearchUsersQuery.class));
    verify(multiHandlerService).handleUserCreated(any(UserCreatedEvent.class));
    verify(multiHandlerService).handleUserUpdated(any(UserUpdatedEvent.class));

    assertThat(userId).startsWith("user-");
    assertThat(updateResult).startsWith("updated-");
    assertThat(user.id()).isEqualTo("user-789");
    assertThat(search.count()).isEqualTo(1);
  }

  // Helper methods
  @SuppressWarnings("unchecked")
  private <T> void registerCommandHandler(Class<T> messageType, Function<T, ?> handler) {
    dispatcher.registerCommandHandler(
        messageType, (io.github.dispatch4j.core.handler.CommandHandler<T, Object>) handler::apply);
  }

  @SuppressWarnings("unchecked")
  private <T> void registerQueryHandler(Class<T> messageType, Function<T, ?> handler) {
    dispatcher.registerQueryHandler(
        messageType, (io.github.dispatch4j.core.handler.QueryHandler<T, Object>) handler::apply);
  }

  @SuppressWarnings("unchecked")
  private <T> void registerEventHandler(Class<T> messageType, Consumer<T> handler) {
    dispatcher.registerEventHandler(
        messageType, (io.github.dispatch4j.core.handler.EventHandler<T>) handler::accept);
  }

  // Message classes
  @Command
  record CreateUserCommand(String email, String name) {}

  @Command
  record UpdateUserCommand(String userId, String email, String name) {}

  @Query
  record GetUserQuery(String userId) {}

  @Query
  record SearchUsersQuery(String searchTerm, int limit) {}

  @Event
  record UserCreatedEvent(String userId, String email, String name) {}

  @Event
  record UserUpdatedEvent(String userId, String email, String name) {}

  // View models
  record UserView(String id, String email, String name) {}

  record SearchResult(int count, String searchTerm) {}

  // Service class with multiple handlers of each type
  static class MultiHandlerService {

    private static final Logger log = LoggerFactory.getLogger(MultiHandlerService.class);

    @io.github.dispatch4j.core.annotation.CommandHandler
    public String handleCreateUser(CreateUserCommand command) {
      return "user-" + command.email().hashCode();
    }

    @io.github.dispatch4j.core.annotation.CommandHandler
    public String handleUpdateUser(UpdateUserCommand command) {
      return "updated-" + command.userId();
    }

    @io.github.dispatch4j.core.annotation.QueryHandler
    public UserView handleGetUser(GetUserQuery query) {
      return new UserView(query.userId(), "user@example.com", "User Name");
    }

    @io.github.dispatch4j.core.annotation.QueryHandler
    public SearchResult handleSearchUsers(SearchUsersQuery query) {
      return new SearchResult(query.searchTerm().equals("search") ? 1 : 2, query.searchTerm());
    }

    @io.github.dispatch4j.core.annotation.EventHandler
    public void handleUserCreated(UserCreatedEvent event) {
      log.info("User created: {}", event.userId());
    }

    @io.github.dispatch4j.core.annotation.EventHandler
    public void handleUserUpdated(UserUpdatedEvent event) {
      log.info("User updated: {}", event.userId());
    }
  }
}
