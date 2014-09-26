package com.opower.connectionpool;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class PoolCleanUpTaskTest {
    private PoolCleanUpTask poolCleanUpTask;
    private ConnectionPool connectionPool;

    @Before
    public void setUp() {
        poolCleanUpTask = new PoolCleanUpTask();
        connectionPool = EasyMock.createMock(ConnectionPool.class);
    }

    @Test
    public void test() throws InterruptedException {
        connectionPool.recycleUnusableConnections();
        EasyMock.replay(connectionPool);
        poolCleanUpTask.scheduleRecycle(connectionPool, 60);
        Thread.sleep(25000);
        EasyMock.verify(connectionPool);
    }

}
