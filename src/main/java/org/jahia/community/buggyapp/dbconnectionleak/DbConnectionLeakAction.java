package org.jahia.community.buggyapp.dbconnectionleak;

import java.util.List;
import javax.sql.DataSource;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.buggyapp.common.AbstractBuggyAction;
import org.jahia.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaks connections from Jahia's own database pool. Once the pool is exhausted, Jahia requests that need the
 * database wait for a connection and fail after the pool timeout ("Timeout: Pool empty").
 */
@Command(scope = "buggy-app", name = "db-connection-leak", description = "Leak connections from the Jahia database pool")
@Service
public class DbConnectionLeakAction extends AbstractBuggyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnectionLeakAction.class);

    @Option(name = "-d", aliases = "--duration", description = "Duration of the leak in ms (target reached at half of it)")
    long duration = 60_000L;

    @Option(name = "-n", aliases = "--connections", description = "Connections to leak (default 0: the whole pool)")
    int connections = 0;

    @Override
    protected Object doExecute() throws InterruptedException {
        requirePositive(duration, "duration");
        if (connections < 0) {
            throw new IllegalArgumentException("number of connections must be >= 0, got " + connections);
        }
        final DataSource dataSource = jahiaDataSource();
        final int target = connections > 0 ? connections : PoolIntrospection.maxActive(dataSource).orElseThrow(
                () -> new IllegalArgumentException("Cannot detect the pool size: pass the number of connections with -n"));
        LOGGER.info("Beginning of the leak: {} connections", target);
        runFor(duration, List.of(new DbConnectionLeakThread(dataSource, target, duration)));
        LOGGER.info("End of the leak");
        return null;
    }

    /** Jahia's datasource; overridable for tests. */
    DataSource jahiaDataSource() {
        final DataSource dataSource = DatabaseUtils.getDatasource();
        if (dataSource == null) {
            throw new IllegalStateException("The Jahia datasource is not available");
        }
        return dataSource;
    }
}
