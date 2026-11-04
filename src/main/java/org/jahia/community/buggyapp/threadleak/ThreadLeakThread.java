package org.jahia.community.buggyapp.threadleak;

import java.io.IOException;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts an async HTTP client, which spawns its own I/O reactor and dispatcher threads (one per CPU core).
 * Those threads are the "leak" until {@link #close()} is called.
 */
public class ThreadLeakThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLeakThread.class);
    private final PoolingNHttpClientConnectionManager connectionManager;
    /** Private lock: never synchronize on a Thread instance, Thread.join() waits on it. */
    private final Object lock = new Object();
    private CloseableHttpAsyncClient asyncHttpClient;
    private boolean closed;

    public ThreadLeakThread(String name) {
        super(name);
        try {
            connectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor());
        } catch (IOReactorException ex) {
            // Previously logged and ignored: the client then built its own manager that was never shut down
            throw new IllegalStateException("Failed to create the I/O reactor", ex);
        }
    }

    @Override
    public void run() {
        synchronized (lock) {
            if (closed) {
                return;
            }
            asyncHttpClient = HttpAsyncClients.custom().setConnectionManager(connectionManager).build();
            asyncHttpClient.start();
        }
    }

    /** Stops the client and its reactor threads. Safe to call before, during or after {@link #run()}. */
    public void close() {
        synchronized (lock) {
            closed = true;
            if (asyncHttpClient != null) {
                try {
                    asyncHttpClient.close();
                } catch (IOException ex) {
                    LOGGER.error("Failed to close the async HTTP client", ex);
                }
            }
            try {
                connectionManager.shutdown();
            } catch (IOException ex) {
                LOGGER.error("Failed to shut down the connection manager", ex);
            }
        }
    }
}
