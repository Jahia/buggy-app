package org.jahia.community.buggyapp.threadleak;

import java.io.IOException;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLeakThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLeakThread.class);
    private transient PoolingNHttpClientConnectionManager connectionManager = null;
    private transient CloseableHttpAsyncClient asyncHttpClient = null;

    public ThreadLeakThread() {
        final DefaultConnectingIOReactor ioReactor;
        try {
            ioReactor = new DefaultConnectingIOReactor();
            connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        } catch (IOReactorException ex) {
            LOGGER.error("Failed to create connection Manager due to: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void run() {
        this.asyncHttpClient = HttpAsyncClients.custom().setConnectionManager(connectionManager).build();
        this.asyncHttpClient.start();
    }

    public void closeUnderlyingConnections() {
        if (connectionManager != null) {
            try {
                connectionManager.shutdown();
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
