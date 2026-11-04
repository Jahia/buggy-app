package org.jahia.community.buggyapp.common;

import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of every buggy-app command.
 *
 * <ul>
 * <li>Logs a warning when a fault is injected on a Jahia running in production mode. It does not refuse: the
 * training environment on Jahia Cloud runs in production mode and the exercise commands must work as written.</li>
 * <li>Provides {@link #runFor(long, List)}, which always stops the workers, even when the command is interrupted
 * (Ctrl-C or closed SSH session in the Karaf shell).</li>
 * </ul>
 */
public abstract class AbstractBuggyAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBuggyAction.class);
    private static final long JOIN_TIMEOUT_MS = 5_000L;

    @Override
    public final Object execute() throws Exception {
        if (isProductionMode()) {
            LOGGER.warn("buggy-app: injecting fault '{}' on a Jahia running in production mode",
                    getClass().getSimpleName());
        }
        return doExecute();
    }

    protected abstract Object doExecute() throws Exception;

    protected boolean isProductionMode() {
        final SettingsBean settings = SettingsBean.getInstance();
        return settings != null && settings.isProductionMode();
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
