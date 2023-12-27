package org.example;
import org.example.models.*;
import org.example.sql.DatabaseConnectionManagment;
import org.example.sql.PostgresSqlConnectionManagement;

import java.sql.SQLException;

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
        DatabaseConnectionManagment dam = new PostgresSqlConnectionManagement("localhost", 5432, "ticket", "postgres", "postgres");
        User userModel = new User();
        userModel.setConnectionManager(dam);

        Object[] values = {};
        Object[] havingValues = {};
        String[] groupColumns = {"role"};

        User user = userModel.getFirst("users", 3);
        System.out.println(user);
//        for(Review t: user.getReviews()){
//            System.out.println(t);
//        }

    }
}