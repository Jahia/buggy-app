package org.jahia.community.buggyapp.backgroundnoise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BackgroundNoiseTest {

    private static final long TIMEOUT_MS = 10_000L;

    @AfterEach
    void stopAnyRunningNoise() throws Exception {
        stopAction().execute();
    }

    @Test
    void producersFillTheStoreAndTheConsumerEmptiesItPeriodically() throws InterruptedException {
        // Arrange: 3 producers, store emptied every 100 ms
        final BackgroundNoise noise = new BackgroundNoise(60_000L, 3, 100L);

        // Act
        noise.start();
        waitFor(() -> noise.getStoreSize() > 50);
        final long producedBefore = noise.getProduced();
        waitFor(() -> noise.getClears() >= 2);

        // Assert
        assertEquals(4, noiseWorkers().count(), "3 producers + 1 consumer");
        assertTrue(noise.getProduced() > producedBefore, "producers keep allocating");
        assertTrue(noise.getClears() >= 2, "the consumer empties the store periodically");

        noise.doStop();
        noise.join(TIMEOUT_MS);
        waitFor(() -> noiseWorkers().findAny().isEmpty());
        assertFalse(noise.isAlive());
        assertFalse(noiseWorkers().findAny().isPresent(), "all noise threads must be stopped");
        assertEquals(0, noise.getStoreSize(), "the retained objects are released");
    }

    @Test
    void endsOnItsOwnAfterTheDuration() throws InterruptedException {
        final BackgroundNoise noise = new BackgroundNoise(200L, 2, 50L);

        noise.start();
        noise.join(TIMEOUT_MS);

        assertFalse(noise.isAlive());
        waitFor(() -> noiseWorkers().findAny().isEmpty());
        assertFalse(noiseWorkers().findAny().isPresent());
    }

    @Test
    void retainedObjectsAreCapped() {
        assertTrue(BackgroundNoise.MAX_RETAINED_ENTRIES > 0);
    }

    @Test
    void rejectsInvalidOptions() {
        assertThrows(IllegalArgumentException.class, () -> new BackgroundNoise(0L, 1, 100L));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundNoise(1_000L, 0, 100L));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundNoise(1_000L, 1, 0L));
    }

    @Test
    void actionRunsInBackgroundOneAtATimeAndCanBeStopped() throws Exception {
        final BackgroundNoiseAction start = new BackgroundNoiseAction();
        start.duration = 60_000L;

        assertTrue(String.valueOf(start.execute()).contains("started"));
        assertTrue(String.valueOf(new BackgroundNoiseAction().execute()).contains("already running"));
        assertEquals("Background noise stopped", stopAction().execute());
        assertEquals("No background noise is running", stopAction().execute());
    }

    private static BackgroundNoiseAction stopAction() {
        final BackgroundNoiseAction action = new BackgroundNoiseAction();
        action.stop = true;
        return action;
    }

    private static Stream<Thread> noiseWorkers() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(t -> t instanceof NoiseProducer || t instanceof NoiseConsumer);
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10L);
        }
    }
}
