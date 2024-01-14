package org.example;
//import org.example.mapper.ORMManagement;
import org.example.mapper.ORMManagement;
import org.example.models.*;
import org.example.sql.CRUDManager;
import org.example.sql.DatabaseConnectionManagment;
import org.example.sql.PostgresSqlConnectionManagement;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        // SETUP ORM LIBRARY
        ORMManagement.setPackageName("org.example");
        ORMManagement.save();

        // SETUP DATABASE CONNECTION
        DatabaseConnectionManagment dcm = new PostgresSqlConnectionManagement(
                "localhost",
                5432,
                "ticket",
                "postgres",
                "localdb");

        CRUDManager cm = new CRUDManager(dcm);

        // QUERY
        List<User> uList = cm.executeSelect(cm.sqlBuidler()
                .select()
                .from("users")
                .limit(2)
                .result(),null, 2);

        for(User t: uList){
            for(Review r: t.getReviews()){
                System.out.println(r);
            }
        }

//        Object[] conditionValues = {0};
//        String[] groupColumns = {"role"};
//
//        List<User> uList = cm.executeSelect(cm.sqlBuidler()
//                .select()
//                .selectedColumn(groupColumns)
//                .from("users")
//                .groupBy(groupColumns)
//                .limit(3)
//                .result(),null, 1);


//        Blog b = new Blog("Vui là chính", "Test", "Test");
//
//        cm.executeInsert(b);

    }
}