package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit tests for the ConnectionPoolImpl class. hsqldb is used as an in-memory database for testing purposes
 * 
 * @author Nikhil Desai
 * 
 */
public class ConnectionPoolImplTest {
    private ConnectionPoolImpl connectionPool;

    // -------------- Constructor tests --------------------

    @Test
    public void test_constructor_valid_args() throws SQLException {
        new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 10, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_constructor_invalid_init_pool_size() throws SQLException {
        new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_constructor_invalid_max_pool_size() throws SQLException {
        new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_constructor_null_username() throws SQLException {
        new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", null, "", 5, 0);
    }

    // -------------- Tests for ConnectionPoolImpl.getConnection() --------------------

    @Test
    public void testGetConnection_create_new_connection() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        Object object = connectionPool.getConnection();
        Assert.assertTrue(object instanceof Connection);
        Assert.assertTrue(((Connection) object).isValid(0));
    }

    @Test
    public void testGetConnection_re_use_existing() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        Connection connection1 = connectionPool.getConnection();
        connectionPool.releaseConnection(connection1);
        Connection connection2 = connectionPool.getConnection();
        Assert.assertTrue(connection1.equals(connection2));

    }

    @Test(expected = SQLException.class)
    public void testGetConnection_create_new_connection_throws_exception_invalid_username() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "", "", 1, 0);
        connectionPool.getConnection();
    }

    @Test
    public void testGetConnection_exceed_pool_size() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 1);
        connectionPool.getConnection();
        try {
            connectionPool.getConnection();
            Assert.fail("Should have thrown SQLException");
        } catch (SQLException se) {
            Assert.assertEquals(se.getMessage(), "No more connections available");
        } catch (Exception e) {
            Assert.fail("Should have thrown SQLException");
        }
    }

    @Test
    public void testGetConnection_two_threads() throws SQLException {
        final ConnectionPool connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 20, 20);

        new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        connectionPool.getConnection();
                    } catch (SQLException e) {
                        Assert.fail();
                    }
                }
            }
        }.run();

        new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        connectionPool.getConnection();
                    } catch (SQLException e) {
                        Assert.fail();
                    }
                }
            }
        }.run();
    }

    // -------------- Tests for ConnectionPoolImpl.releaseConnection() --------------------
    @Test(expected = IllegalArgumentException.class)
    public void testReleaseConnection_null_argument() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        connectionPool.releaseConnection(null);
    }

    @Test
    public void testReleaseConnection_new_connection_then_release() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        connectionPool.releaseConnection(connectionPool.getConnection());
    }

    @Test
    public void testReleaseConnection_connection__available_then_released() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 1);
        connectionPool.releaseConnection(connectionPool.getConnection());
    }

    @Test
    public void testReleaseConnection_releasing_twice() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 1);
        Connection connection = connectionPool.getConnection();
        connectionPool.releaseConnection(connection);
        connectionPool.releaseConnection(connection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseConnection_releasing_connection_not_created_by_same_pool() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 1);
        ConnectionPool differentConnectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        Connection connection = connectionPool.getConnection();
        differentConnectionPool.releaseConnection(connection);
    }

    // -------------- Tests for ConnectionPoolImpl.recycleAvailableConnections() --------------------
    @Test
    public void testRecycleAvailableConnections_recycle_closed_connection() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 1);
        Connection connection1 = connectionPool.getConnection();
        connection1.close();
        connectionPool.releaseConnection(connection1);
        connectionPool.recycleUnusableConnections();
        Connection connection2 = connectionPool.getConnection();
        Assert.assertTrue(!connection2.isClosed());
        Assert.assertTrue(!connection2.equals(connection1));
    }

    @Test
    public void testRecycleAvailableConnections_recycle_works_when_list_empty() throws SQLException {
        connectionPool = new ConnectionPoolImpl("jdbc:hsqldb:mem:aname", "sa", "", 1, 0);
        connectionPool.recycleUnusableConnections();
    }
}