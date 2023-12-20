package org.example;
import org.example.models.*;
import org.example.sql.DatabaseAccessManagment;
import org.example.sql.PostgresSqlAccessManagement;
import org.example.sql.SqlBuilder;

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
        DatabaseAccessManagment dam = new PostgresSqlAccessManagement("localhost", 5432, "ticket", "postgres", "localdb");
        User userModel = new User();
        userModel.setConnectionManager(dam);

        Object[] values = {};

        User user = userModel.getFirst("users", "true", values, 3);

        for(Review t: user.getReviews()){
            System.out.println(t.getUser());
        }

    }
}