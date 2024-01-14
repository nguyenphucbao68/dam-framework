package org.demo;
import org.demo.models.Blog;
import org.example.mapper.ActiveRecord;
import org.example.mapper.ORMManagement;
import org.example.sql.CRUDManager;
import org.example.sql.DatabaseConnectionManagment;
import org.example.sql.PostgresSqlConnectionManagement;
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
                "postgres");

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


        ActiveRecord b = new Blog(
                UUID.fromString("c118f693-8722-4461-a79d-d76991b96fdf"),
                "Test Blog 2",
                "This is a test blog",
                "https://www.google.com"
        );

        cm.executeDelete(b);

    }
}