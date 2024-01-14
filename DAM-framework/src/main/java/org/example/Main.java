package org.example;
import org.example.models.*;
import org.example.sql.CRUDManager;
import org.example.sql.DatabaseConnectionManagment;
import org.example.sql.PostgresSqlConnectionManagement;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
/*
        // OneToOne
        Review reviewModel = new Review();
        Object[] values = {};

        Review review = reviewModel.getFirst("reviews", "true", values);

        System.out.println(review.getUser().toString());
*/

        // OneToMany
        DatabaseConnectionManagment dcm = new PostgresSqlConnectionManagement("localhost", 5432, "ticket", "postgres", "localdb");
        CRUDManager cm = new CRUDManager(dcm);

        Object[] conditionValues = {0};
        String[] groupColumns = {"role"};

        //User a = new User(....)
        //cm.executeInsert(a);


//        List<User> uList = cm.executeSelect(cm.sqlBuidler()
//                .select()
//                .selectedColumn(groupColumns)
//                .from("users")
//                .groupBy(groupColumns)
//                .limit(3)
//                .result(),null, 1);
//        List<User> uList = cm.executeSelect(cm.sqlBuidler()
//                .select()
//                .from("users")
//                .limit(2)
//                .result(),null, 1);

//        int i = 0;
//        for(User t: uList){
//            System.out.println(t);
//            i++;
//            if(i == 2){
//                System.out.println(t);
//                cm.executeDelete(t);
//            }
//            t.setDisplayName("Test");
//
//            cm.executeUpdate(t);
//        }
        Blog b = new Blog("Test", "Test", "Test");

        cm.executeInsert(b);

    }
}