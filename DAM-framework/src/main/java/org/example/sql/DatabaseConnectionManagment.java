package org.example.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseConnectionManagment {
    protected String hostname = null;
    protected Integer port = 0;
    protected String databaseName = null;
    protected String user = "";
    protected String password = "";
    private int maxPool = 5;
    private List<Connection> connectionPool = new ArrayList<>();

    protected DatabaseConnectionManagment(String hostname, int port, String databaseName, String user, String password){
        this.hostname = hostname;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }
    public Connection getConnection() throws SQLException {
        if(connectionPool.size() < maxPool){
            Connection con = DriverManager.getConnection(getConnectionUrl(), user, password);
            connectionPool.add(con);
            return con;
        }else
            throw new SQLException("Max connection pool");
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            if(!connection.isClosed())
                connection.close();
            connectionPool.remove(connection);
        }
    }
    public int getMaxPool() {
        return maxPool;
    }
    public void setMaxPool(int maxPool) {
        this.maxPool = maxPool;
    }
    abstract protected String getConnectionUrl();
}
