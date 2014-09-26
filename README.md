dbconnectionpool
================

Summary:

This repo is an implementation of a DB Connection pool. Here are a few details about the implementation:

- A new pool can be created by instantiating the ConnectionPoolImpl class. 
- A new connection can be obtained by calling ConnectionPoolImpl.getConnection()
- A connection created by this pool can be released back to the pool by calling ConnectionPoolImpl.releaseConnection(connection)
- When a connection pool is created, it creates an instance of PoolCleanUpTask that schedules execution of ConnectionPoolImpl.recycleUnusableConnections() which recycles unusable connections in the pool

To get a local copy, use:
git clone 

Command to run unit tests:

mvn clean test
