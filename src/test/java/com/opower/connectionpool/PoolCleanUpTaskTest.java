package com.opower.connectionpool;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the PoolCleanUpTask class.
 * 
 * @author Nikhil Desai
 * 
 */
public class PoolCleanUpTaskTest {
    private PoolCleanUpTask poolCleanUpTask;
    private ConnectionPool connectionPool;

    @Before
    public void setUp() {
        poolCleanUpTask = new PoolCleanUpTask();
        connectionPool = EasyMock.createMock(ConnectionPool.class);
    }

    @Test
    public void testScheduleRecycle_normal_case() throws InterruptedException {
        connectionPool.recycleUnusableConnections();
        EasyMock.replay(connectionPool);
        poolCleanUpTask.scheduleRecycle(connectionPool, 60);
        Thread.sleep(2000); // wait 2 seconds so that the connectionPool.recycleUnusableConnections() has been executed
                            // on a separate thread
        EasyMock.verify(connectionPool);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScheduleRecycle_null_pool() throws InterruptedException {
        poolCleanUpTask.scheduleRecycle(null, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScheduleRecycle_invalid_time_period() throws InterruptedException {
        connectionPool.recycleUnusableConnections();
        EasyMock.replay(connectionPool);
        poolCleanUpTask.scheduleRecycle(connectionPool, -2);
        EasyMock.verify(connectionPool);
    }

}
