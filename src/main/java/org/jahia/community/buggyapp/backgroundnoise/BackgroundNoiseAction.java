package org.jahia.community.buggyapp.backgroundnoise;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.jahia.community.buggyapp.common.BackgroundScenario;

/**
 * Starts the background noise in background. Only one instance runs at a time; {@code --stop} ends it early.
 */
@Command(scope = "buggy-app", name = "background-noise",
        description = "Busy but healthy background activity, to run alongside another scenario (runs in background)")
@Service
public class BackgroundNoiseAction extends AbstractBuggyAction {

    private static final BackgroundScenario SCENARIO =
            new BackgroundScenario("background noise", "Background noise stopped");

    @Option(name = "-d", aliases = "--duration", description = "Duration of the noise in ms")
    long duration = 600_000L;
    @Option(name = "-p", aliases = "--producers", description = "Number of producer threads")
    int producers = 5;
    @Option(name = "-c", aliases = "--clear-interval", description = "Interval in ms between two clears of the objects")
    long clearInterval = 10_000L;
    @Option(name = "--stop", description = "Stop the running background noise")
    boolean stop = false;

    @Override
    protected Object doExecute() throws InterruptedException {
        if (stop) {
            return SCENARIO.stop();
        }
        // Options are validated by the constructor, so bad input fails in the shell, not in the background
        return SCENARIO.start(new BackgroundNoise(duration, producers, clearInterval));
    }
}
