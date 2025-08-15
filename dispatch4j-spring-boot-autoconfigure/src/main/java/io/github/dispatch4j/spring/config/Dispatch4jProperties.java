package io.github.dispatch4j.spring.config;

import io.github.dispatch4j.discovery.ConflictResolutionStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Dispatch4j Spring Boot integration.
 *
 * <p>These properties allow customization of the Dispatch4j behavior through Spring Boot's
 * configuration system. All properties use the {@code dispatch4j} prefix.
 *
 * <p>Available configuration options:
 *
 * <ul>
 *   <li>{@code dispatch4j.enabled}: Enable/disable auto-configuration (default: true)
 *   <li>{@code dispatch4j.delegate-security-context}: Wrap executor with security context
 *       delegation (default: true)
 *   <li>{@code dispatch4j.async.*}: Async executor configuration options
 *   <li>{@code dispatch4j.middleware.*}: Middleware configuration options
 *   <li>{@code dispatch4j.discovery.*}: Handler discovery configuration options
 * </ul>
 *
 * <p>Example application.yml configuration:
 *
 * <pre>{@code
 * dispatch4j:
 *   enabled: true
 *   delegate-security-context: true
 *   async:
 *     core-pool-size: 4
 *     max-pool-size: 8
 *     queue-capacity: 1000
 *   middleware:
 *     logging-enabled: true
 *    discovery:
 *      conflict-resolution: MERGE_ALL
 * }</pre>
 */
@ConfigurationProperties(prefix = "dispatch4j")
public class Dispatch4jProperties {

    private boolean enabled = true;
    private String customExecutorBeanName;
    private boolean delegateSecurityContext = true;
    private Async async = new Async();
    private Discovery discovery = new Discovery();
    private Middleware middleware = new Middleware();

    /**
     * Gets whether Dispatch4j auto-configuration is enabled.
     *
     * @return true if auto-configuration is enabled (default: true)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether Dispatch4j auto-configuration is enabled.
     *
     * @param enabled true to enable auto-configuration
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the custom executor bean name.
     *
     * @return the custom executor bean name, or null if using default
     */
    public String getCustomExecutorBeanName() {
        return customExecutorBeanName;
    }

    /**
     * Sets the custom executor bean name.
     *
     * @param customExecutorBeanName the custom executor bean name
     */
    public void setCustomExecutorBeanName(String customExecutorBeanName) {
        this.customExecutorBeanName = customExecutorBeanName;
    }

    /**
     * Gets whether security context delegation is enabled.
     *
     * @return true if security context delegation is enabled (default: true)
     */
    public boolean isDelegateSecurityContext() {
        return delegateSecurityContext;
    }

    /**
     * Sets whether security context delegation is enabled.
     *
     * @param delegateSecurityContext true to enable security context delegation
     */
    public void setDelegateSecurityContext(boolean delegateSecurityContext) {
        this.delegateSecurityContext = delegateSecurityContext;
    }

    /**
     * Gets the async executor configuration.
     *
     * @return the async configuration properties
     */
    public Async getAsync() {
        return async;
    }

    /**
     * Sets the async executor configuration.
     *
     * @param async the async configuration properties
     */
    public void setAsync(Async async) {
        this.async = async;
    }

    /**
     * Gets the middleware configuration.
     *
     * @return the middleware configuration properties
     */
    public Middleware getMiddleware() {
        return middleware;
    }

    /**
     * Sets the middleware configuration.
     *
     * @param middleware the middleware configuration properties
     */
    public void setMiddleware(Middleware middleware) {
        this.middleware = middleware;
    }

    /**
     * Gets the handler discovery configuration.
     *
     * @return the discovery configuration properties
     */
    public Discovery getDiscovery() {
        return discovery;
    }

    /**
     * Sets the handler discovery configuration.
     *
     * @param discovery the discovery configuration properties
     */
    public void setDiscovery(Discovery discovery) {
        this.discovery = discovery;
    }

    /**
     * Configuration properties for the async executor used by Dispatch4j.
     *
     * <p>These properties control the {@link
     * org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor} that handles asynchronous
     * command, query, and event processing.
     */
    public static class Async {
        private int corePoolSize = Runtime.getRuntime().availableProcessors();
        private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;
        private int queueCapacity = 1000;
        private String threadNamePrefix = "dispatch4j-";

        /**
         * Gets the core pool size for the async executor.
         *
         * @return the core pool size (default: number of available processors)
         */
        public int getCorePoolSize() {
            return corePoolSize;
        }

        /**
         * Sets the core pool size for the async executor.
         *
         * @param corePoolSize the core pool size
         */
        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        /**
         * Gets the maximum pool size for the async executor.
         *
         * @return the maximum pool size (default: 2x number of available processors)
         */
        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        /**
         * Sets the maximum pool size for the async executor.
         *
         * @param maxPoolSize the maximum pool size
         */
        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        /**
         * Gets the queue capacity for the async executor.
         *
         * @return the queue capacity (default: 1000)
         */
        public int getQueueCapacity() {
            return queueCapacity;
        }

        /**
         * Sets the queue capacity for the async executor.
         *
         * @param queueCapacity the queue capacity
         */
        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        /**
         * Gets the thread name prefix for the async executor.
         *
         * @return the thread name prefix (default: "dispatch4j-")
         */
        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        /**
         * Sets the thread name prefix for the async executor.
         *
         * @param threadNamePrefix the thread name prefix
         */
        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    /**
     * Configuration properties for middleware used by Dispatch4j.
     *
     * <p>These properties control which built-in middleware components are enabled and their
     * configuration.
     */
    public static class Middleware {
        private boolean loggingEnabled = false;

        /**
         * Gets whether logging middleware is enabled.
         *
         * @return true if logging middleware should be auto-configured (default: false)
         */
        public boolean isLoggingEnabled() {
            return loggingEnabled;
        }

        /**
         * Sets whether logging middleware is enabled.
         *
         * @param loggingEnabled true to enable logging middleware
         */
        public void setLoggingEnabled(boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }
    }

    public static class Discovery {
        private ConflictResolutionStrategy conflictResolution =
                ConflictResolutionStrategy.FAIL_FAST;

        /**
         * Gets the conflict resolution strategy for handler discovery.
         *
         * <p>This controls how conflicts between discovered handlers are resolved. Default is
         * {@link ConflictResolutionStrategy#FAIL_FAST}.
         *
         * @return the conflict resolution strategy
         */
        public ConflictResolutionStrategy getConflictResolution() {
            return conflictResolution;
        }

        /**
         * Sets the conflict resolution strategy for handler discovery.
         *
         * <p>This controls how conflicts between discovered handlers are resolved. Default is
         * {@link ConflictResolutionStrategy#FAIL_FAST}.
         *
         * @param conflictResolution the conflict resolution strategy to use
         */
        public void setConflictResolution(ConflictResolutionStrategy conflictResolution) {
            this.conflictResolution = conflictResolution;
        }
    }
}
