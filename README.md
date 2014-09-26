dbconnectionpool
================

Summary:

This repo is an implementation of a DB Connection pool. Here are a few details about the implementation:

- A new pool can be created by instantiating the ConnectionPoolImpl class. 
- A new connection can be obtained by calling ConnectionPoolImpl.getConnection()
- A connection created by this pool can be released back to the pool by calling ConnectionPoolImpl.releaseConnection(connection)
- When a connection pool is created, it creates an instance of PoolCleanUpTask that schedules execution of ConnectionPoolImpl.recycleUnusableConnections() which recycles unusable connections in the pool


Key Data Structures used:

-A CopyOnWriteArrayList is used to hold the available connections. This collection provides random access as well as thread safety for add() and remove() operations. 
ref: http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/CopyOnWriteArrayList.html


Tests:

- The ConnectionPoolImplTest class contains unit tests for methods in ConnectionPoolImpl. Some of these tests hsqldb as an in-memory database
- The PoolCleanUpTaskTest class contains test for PoolCleanUpTask. EasyMock is used here to create a mock ConnectionPool.


Logs:

- The app log is saved in a file called app.log in the root directory of the project. The log4j configuration is under src/main/resources/log4j.properties


To get a local copy of the project, use:

 <code>git clone https://github.com/nikhildesai/dbconnectionpool.git </code>


Pre-requisites:

<code>maven 3.0.3+<br>
JDK 1.6+ </code>


To compile the code, go to the project root directory and use:

 <code>mvn clean compile </code>


To run unit tests, go to the project root directory and use:

 <code>mvn clean test </code>


If you want to import the project in eclipse, you can use the following to generate eclipse-related files:

 <code>mvn eclipse:clean eclipse:eclipse</code>
