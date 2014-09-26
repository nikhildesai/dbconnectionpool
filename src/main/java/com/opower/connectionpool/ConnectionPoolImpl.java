package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ConnectionPoolImpl implements ConnectionPool {
    private Logger logger;
    private String url;
    private String username;
    private String password;
    /**
     * Max number of connections in the pool
     */
    private int maxPoolSize;
    /**
     * Number of currently used connections
     */
    private int currentPoolSize;
    /**
     * List of connections released back to the pool
     */
    private List<Connection> availableConnections;
    /**
     * Complete list of connections in the pool at any given time
     */
    private List<Connection> allConnections;

    /**
     * Constructor for the connection pool
     * 
     * @param url
     *            for the db
     * @param username
     * @param password
     * @param maxPoolSize
     *            Max number of connections in the pool
     * @param initialPoolSize
     *            Number of connections in the pool when it is created
     * @throws SQLException
     */
    public ConnectionPoolImpl(String url, String username, String password, int maxPoolSize, int initialPoolSize)
            throws SQLException {
        logger = Logger.getLogger(ConnectionPoolImpl.class);

        validate(url, username, password, maxPoolSize, initialPoolSize);

        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        currentPoolSize = 0;

        // using a CopyOnWriteArrayList so that we can avoid using synchronized methods
        availableConnections = new CopyOnWriteArrayList<Connection>();

        allConnections = new ArrayList<Connection>();

        for (int i = 0; i < initialPoolSize; i++) {
            availableConnections.add(createNewConnection());
        }

        logger.log(Level.INFO, "Connection pool initialized");

        // start task to clean up available connections
        PoolCleanUpTask cleanUpTask = new PoolCleanUpTask();
        cleanUpTask.scheduleRecycle(this, 60l);
    }

    private void validate(String url, String username, String password, int maxPoolSize, int initialPoolSize) {
        if (username == null || password == null || url == null) {
            throw new IllegalArgumentException("Url, Username Or Password cannot be null");
        }

        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be a positive integer");
        }

        if ((initialPoolSize < 0) || (initialPoolSize > maxPoolSize)) {
            throw new IllegalArgumentException(
                    "initialPoolSize must be a positive integer less than or equal to maxPoolSize");
        }

    }

    @Override
    public Connection getConnection() throws SQLException {
        // 1. Re-use an available connection
        if (availableConnections.size() > 0) {
            logger.log(Level.DEBUG, "Re-using existing connection");
            final Connection connection = availableConnections.remove(0);
            return connection;
        }

        // 2. Create a connection if needed
        return createNewConnection();
    }

    private Connection createNewConnection() throws SQLException {
        checkIfMaxPoolSizeReached();

        try {
            final Connection connection = DriverManager.getConnection(url, username, password);
            currentPoolSize++;
            allConnections.add(connection);
            logger.log(Level.DEBUG, "currentPoolSize = " + currentPoolSize);
            return connection;
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Exception while creating a new connection using DriverManager " + e);
            throw e;
        }
    }

    private void checkIfMaxPoolSizeReached() throws SQLException {
        if (currentPoolSize >= maxPoolSize) {
            logger.log(Level.DEBUG, "No more connections available");
            throw new SQLException("No more connections available");
        }
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection == null) {
            logger.log(Level.WARN, "connection cannot be null");
            throw new IllegalArgumentException("connection cannot be null");
        }

        if (!allConnections.contains(connection)) {
            logger.log(Level.WARN, "connection must have been created by this pool");
            throw new IllegalArgumentException("connection must have been created by this pool");
        }

        if (availableConnections.contains(connection)) {
            logger.log(Level.WARN, "Connection already released. Ignoring ...");
            return;
        }

        availableConnections.add(connection);
        logger.log(Level.DEBUG, "Released connection added back to the pool");
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("Connection Pool: ");
        stringBuffer.append(" maxPoolSize= ").append(maxPoolSize);
        stringBuffer.append(" currentPoolSize= ").append(currentPoolSize);
        stringBuffer.append(" dburl= ").append(url);
        return stringBuffer.toString();
    }

    /**
     * Iterates over the list of available connections and checks if they are still valid
     * 
     * @throws SQLException
     */
    public synchronized void recycleUnusableConnections() {
        List<Connection> unusableConnections = new ArrayList<Connection>();
        for (Connection connection : availableConnections) {
            try {
                if (!connection.isValid(0) || connection.isClosed()) {
                    unusableConnections.add(connection);
                }
            } catch (SQLException e) {
                // add the connection to the unusable list even if there is an exception
                unusableConnections.add(connection);
            }
        }
        availableConnections.removeAll(unusableConnections);
        allConnections.removeAll(unusableConnections);
        currentPoolSize = currentPoolSize - unusableConnections.size();
    }
}