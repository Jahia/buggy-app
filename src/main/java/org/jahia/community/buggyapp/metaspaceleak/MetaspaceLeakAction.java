package org.jahia.community.buggyapp.metaspaceleak;

import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "metaspace-leak", description = "Metaspace leak")
@Service
public class MetaspaceLeakAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms")
    long duration = 60_000L;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        final MetaspaceLeakThread thread = new MetaspaceLeakThread();
        LOGGER.info("Beginning of the leak");
        runFor(duration, List.of(thread));
        LOGGER.info("End of the leak: {} classes created", thread.getClassesCreated());
        return null;
    }
}
