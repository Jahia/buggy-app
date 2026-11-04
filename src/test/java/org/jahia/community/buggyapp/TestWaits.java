package org.jahia.community.buggyapp;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * Waits used by the scenario tests, without {@code Thread.sleep}: polling for a condition, or letting a scenario
 * run for a while before observing or stopping it.
 */
public final class TestWaits {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(10);

    private TestWaits() {
    }

    /** Waits until {@code condition} is true; fails the test after 10 s. */
    public static void waitFor(BooleanSupplier condition) {
        waitFor(DEFAULT_TIMEOUT, condition);
    }

    /** Waits until {@code condition} is true; fails the test after {@code timeout}. */
    public static void waitFor(Duration timeout, BooleanSupplier condition) {
        await().atMost(timeout).pollInterval(POLL_INTERVAL).until(condition::getAsBoolean);
    }

    /** Lets the scenario under test run for {@code millis} ms. */
    public static void letRun(long millis) {
        final Duration delay = Duration.ofMillis(millis);
        await().pollDelay(delay).atMost(delay.plus(DEFAULT_TIMEOUT)).until(() -> true);
    }
}
