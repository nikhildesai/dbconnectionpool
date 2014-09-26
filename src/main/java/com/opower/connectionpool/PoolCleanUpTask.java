package com.opower.connectionpool;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class PoolCleanUpTask {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Logger logger = Logger.getLogger(PoolCleanUpTask.class);

    /**
     * Schedules a period task to recycle the connection pool
     * 
     * @param connectionPool
     *            connection pool to be recycled
     * @param timePeriodSeconds
     *            the period between successive executions in seconds
     */
    public void scheduleRecycle(final ConnectionPool connectionPool, final long timePeriodSeconds) {
        final Runnable poolCleanUpRunnable = new Runnable() {
            public void run() {
                logger.log(Level.DEBUG, "Recycling available connections ...");
                connectionPool.recycleUnusableConnections();
                logger.log(Level.DEBUG, "Done recycling available connections");
            }
        };
        scheduler.scheduleAtFixedRate(poolCleanUpRunnable, 10, timePeriodSeconds, SECONDS);
    }
}