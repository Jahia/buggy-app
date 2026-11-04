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
