package org.example.sql;

import java.sql.SQLException;

public class CRUDManager {
    private DatabaseAccessManagment dam;
    public CRUDManager(DatabaseAccessManagment dam){
        this.dam = dam;
    }
    public int insert(Object obj) throws SQLException {
        //Connection connection = dam.getConnection();
        //PreparedStatement statement = connection.prepareStatement("INSERT INTO ?(?) VALUE(?)");

        checkNull(obj);

        //check table name
        //String tableName =

        return 0;
    }
//    private String getTableName(Object obj){
//        Class<?> clazz = obj.getClass();
//        if (!clazz.isAnnotationPresent(Table.class)) {
//            throw new JsonSerializationException("The class "
//                    + clazz.getSimpleName()
//                    + " is not annotated with JsonSerializable");
//        }
//
//    }
    private void checkNull(Object obj) throws SQLException {
        if(obj == null){
            throw new SQLException("Object is null");
        }
    }
}
