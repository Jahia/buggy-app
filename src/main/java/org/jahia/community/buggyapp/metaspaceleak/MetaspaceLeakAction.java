package org.jahia.community.buggyapp.metaspaceleak;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "buggy-app", name = "metaspace-leak", description = "Metaspace leak")
@Service
public class MetaspaceLeakAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakAction.class);
    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak")
    private long duration = 60000L;

    public static void main(String[] args) throws Exception {

    }

    @Override
    public Object execute() throws Exception {
        LOGGER.info("Beginning of the leak");
        final MetaspaceLeakThread thread = new MetaspaceLeakThread();
        thread.start();
        Thread.sleep(duration);
        thread.doStop();
        LOGGER.info("End of the leak");
        return null;
    }
}
