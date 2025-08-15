package io.github.dispatch4j.discovery;

/** Strategy for resolving conflicts between discovery strategies. */
public enum ConflictResolutionStrategy {
    /** First strategy wins conflicts */
    FIRST_WINS,

    /** Last strategy wins conflicts */
    LAST_WINS,

    /** Fail on any conflict */
    FAIL_FAST,

    /** Allow multiple registrations */
    MERGE_ALL
}
