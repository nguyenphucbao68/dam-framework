package org.example;
import org.example.models.*;

public class Main {
    public static void main(String[] args) {
/*
        // OneToOne
        Review reviewModel = new Review();
        Object[] values = {};

        Review review = reviewModel.getFirst("reviews", "true", values);

        System.out.println(review.getUser().toString());
*/

        // OneToMany
        Object[] values = {};

        User user = User.getFirst("users", "true", values, 3);

        for(Review t: user.getReviews()){
            System.out.println(t.getUser());
        }
    }
}