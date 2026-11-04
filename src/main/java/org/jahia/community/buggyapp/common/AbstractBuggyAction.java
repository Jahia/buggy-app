package org.jahia.community.buggyapp.common;

import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of every buggy-app command.
 *
 * <ul>
 * <li>Refuses to run when Jahia is in production mode unless {@code --force} is given, so the fault injectors
 * cannot be triggered by mistake on a real environment.</li>
 * <li>Provides {@link #runFor(long, List)}, which always stops the workers, even when the command is interrupted
 * (Ctrl-C or closed SSH session in the Karaf shell).</li>
 * </ul>
 */
public abstract class AbstractBuggyAction implements Action {

    static final String PRODUCTION_REFUSAL = "Jahia is running in production mode: refusing to inject a fault."
            + " Re-run the command with --force if this really is a training environment.";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBuggyAction.class);
    private static final long JOIN_TIMEOUT_MS = 5_000L;

    @Option(name = "-f", aliases = "--force", description = "Run even when Jahia is in production mode")
    private boolean force = false;

    @Override
    public final Object execute() throws Exception {
        if (!force && isProductionMode()) {
            LOGGER.warn(PRODUCTION_REFUSAL);
            return PRODUCTION_REFUSAL;
        }
        return doExecute();
    }

    protected abstract Object doExecute() throws Exception;

    protected boolean isProductionMode() {
        final SettingsBean settings = SettingsBean.getInstance();
        return settings != null && settings.isProductionMode();
    }

    void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Fails fast on a non-positive option value, before any thread is started.
     */
    protected static long requirePositive(long value, String optionName) {
        if (value <= 0) {
            throw new IllegalArgumentException(optionName + " must be > 0, got " + value);
        }
        return value;
    }

    /**
     * Starts the workers, waits {@code durationMs}, then stops them. The workers are stopped in every case,
     * including when the calling (shell) thread is interrupted; the interruption is then rethrown.
     */
    protected static void runFor(long durationMs, List<? extends StoppableThread> workers)
            throws InterruptedException {
        try {
            for (StoppableThread worker : workers) {
                worker.start();
            }
            LOGGER.info("{} worker(s) started, waiting {} ms before stopping", workers.size(), durationMs);
            Thread.sleep(durationMs);
        } finally {
            stopAndJoin(workers);
        }
    }

    private static void stopAndJoin(List<? extends StoppableThread> workers) throws InterruptedException {
        for (StoppableThread worker : workers) {
            worker.doStop();
        }
        for (StoppableThread worker : workers) {
            worker.join(JOIN_TIMEOUT_MS);
            if (worker.isAlive()) {
                LOGGER.warn("Worker {} did not stop within {} ms", worker.getName(), JOIN_TIMEOUT_MS);
            }
        }
    }
}
