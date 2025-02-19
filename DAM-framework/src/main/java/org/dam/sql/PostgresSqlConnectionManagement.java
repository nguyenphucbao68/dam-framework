package org.dam.sql;

public class PostgresSqlConnectionManagement extends DatabaseConnectionManagment {
    public PostgresSqlConnectionManagement(String hostname, int port, String databaseName, String user, String password){
        super(hostname,
                port,
                databaseName,
                user,
                password);
        //Class.forName("org.postgresql.Driver");
        //Applications do not need to explicitly load the org.postgresql.Driver class
        //because the pgJDBC driver jar supports the Java Service Provider mechanism.
        //The driver will be loaded by the JVM when the application connects to PostgreSQL
    }
    protected String getConnectionUrl(){
        return "jdbc:postgresql://" + hostname + ":" + port + "/" + databaseName;
    }
}
