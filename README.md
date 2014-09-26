dbconnectionpool
================

Summary:

This repo is an implementation of a DB Connection pool. Here are a few details about the implementation:

- A new pool can be created by instantiating the ConnectionPoolImpl class. 
- A new connection can be obtained by calling ConnectionPoolImpl.getConnection()
- A connection created by this pool can be released back to the pool by calling ConnectionPoolImpl.releaseConnection(connection)
- When a connection pool is created, it creates an instance of PoolCleanUpTask that schedules execution of ConnectionPoolImpl.recycleUnusableConnections() which recycles unusable connections in the pool

To get a local copy, use:

<code>git clone https://github.com/nikhildesai/dbconnectionpool.git</code>

Pre-requisites:

<code>maven 3.0.3+

JDK 1.6+</code>

To run unit tests:

<code>mvn clean test</code>

If you want to import the project in eclipse, you can use:

<code>mvn eclipse:clean eclipse:eclipse</code>
