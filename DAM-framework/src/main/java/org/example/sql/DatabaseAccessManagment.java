package org.example.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseAccessManagment {
    protected String hostname = null;
    protected Integer port = 0;
    protected String databaseName = null;
    protected String user = "";
    protected String password = "";
    public DatabaseAccessManagment(String hostname, int port, String databaseName,String user, String password){
        this.hostname = hostname;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnectionUrl(), user, password);
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    abstract protected String getConnectionUrl();
}
