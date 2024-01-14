package org.demo;
import org.demo.models.Blog;
import org.dam.mapper.ActiveRecord;
import org.dam.mapper.ORMManagement;
import org.dam.sql.CRUDManager;
import org.dam.sql.DatabaseConnectionManagment;
import org.dam.sql.PostgresSqlConnectionManagement;
import org.demo.models.Review;
import org.demo.models.User;

import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // SETUP ORM LIBRARY
        ORMManagement.setPackageName("org.demo");
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


        Blog b = new Blog(
                UUID.fromString("25d9ea5f-5996-4c69-a242-b943a1832d2e"),
                "Test Blog 2",
                "This is a test blog",
                "https://www.google.com"
        );

        cm.executeInsert(b);

        // UPDATE
        b.setTitle("Test Blog 2 Updated");
//
        cm.executeUpdate(b);

        // DELETE
        cm.executeDelete(b);
    }
}