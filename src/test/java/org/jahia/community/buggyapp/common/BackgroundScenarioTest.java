package org.jahia.community.buggyapp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BackgroundScenarioTest {

    /** Runs until stopped. */
    private static final class Idle extends StoppableThread {

        @Override
        public void run() {
            while (keepRunning()) {
                Thread.onSpinWait();
            }
        }
    }

    @Test
    void startsOneInstanceAtATimeAndStopsIt() throws InterruptedException {
        final BackgroundScenario scenario = new BackgroundScenario("noise", "Noise stopped");
        final Idle first = new Idle();

        assertTrue(scenario.start(first).startsWith("Noise started"));
        assertTrue(first.isDaemon(), "background scenarios must never keep the JVM alive");
        assertTrue(scenario.start(new Idle()).contains("already running"));

        assertEquals("Noise stopped", scenario.stop());
        assertFalse(first.isAlive());
        assertEquals("No noise is running", scenario.stop());
    }

    @Test
    void stoppingTheModuleStopsEveryRunningBackgroundScenario() throws InterruptedException {
        // Arrange: two different background scenarios running
        final BackgroundScenario noise = new BackgroundScenario("noise", "Noise stopped");
        final BackgroundScenario leak = new BackgroundScenario("leak", "Leak stopped");
        final Idle noiseThread = new Idle();
        final Idle leakThread = new Idle();
        noise.start(noiseThread);
        leak.start(leakThread);

        // Act: what OSGi calls when the bundle is stopped or redeployed
        new BackgroundScenarioCleaner().deactivate();

        // Assert: otherwise they would survive the redeploy, out of reach of --stop
        assertFalse(noiseThread.isAlive());
        assertFalse(leakThread.isAlive());
    }

    @Test
    void canStartAgainOnceThePreviousRunIsOver() throws InterruptedException {
        final BackgroundScenario scenario = new BackgroundScenario("noise", "Noise stopped");
        scenario.start(new Idle());
        scenario.stop();

        final Idle second = new Idle();
        assertTrue(scenario.start(second).startsWith("Noise started"));
        scenario.stop();
        assertFalse(second.isAlive());
    }
}
