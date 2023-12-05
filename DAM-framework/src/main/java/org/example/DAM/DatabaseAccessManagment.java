package org.example.DAM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseAccessManagment {
    protected String hostname = null;
    protected Integer port = 0;
    protected String databaseName = null;
    protected String user = "";
    protected String password = "";
    protected Connection connection = null;
    public DatabaseAccessManagment(String hostname, int port, String databaseName,String user, String password){
        this.hostname = hostname;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }
    public Connection getConnection() throws SQLException {

        try {
            if (connection != null && connection.isClosed())
                connection = DriverManager.getConnection(getConnectionUrl(), user, password);
            return connection;
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    abstract protected String getConnectionUrl();
}
