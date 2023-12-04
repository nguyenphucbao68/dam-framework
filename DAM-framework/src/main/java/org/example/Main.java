package org.example;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "blogs")
class Blog extends ActiveRecord {
    @PrimaryKey(name = "id", type = "uuid")
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title", type = "varchar")
    private String title;

    @Column(name = "content", type = "text")
    private String content;

    @Column(name = "thumbnail", type = "varchar")
    private String thumbnail;

    @Column(name = "create_time", type = "datetime")
    private Date createdAt;

    @Column(name = "update_time", type = "datetime")
    private Date updatedAt;

    public Blog(String title, String content, String thumbnail) {
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
    }
}

@Table(name = "users")
class User extends ActiveRecord {
    @PrimaryKey(name = "id", type = "uuid")
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "email", type = "varchar")
    private String email;

    @Column(name = "role", type = "varchar")
    private Integer role = 0;

    @Column(name = "display_name", type = "varchar")
    private String display_name;

    @Column(name = "avatar_url", type = "varchar")
    private String avatarUrl;

    @Column(name = "create_time", type = "datetime")
    private Date createdAt;

    @Column(name = "update_time", type = "datetime")
    private Date updatedAt;

    @OneToMany(refTable = "reviews", refColumn = "user_id", joinColumn = "id")
    private List<Review> reviews;

    public List<Review> getReviews() {
        return reviews;
    }
}

@Table(name = "reviews")
class Review extends ActiveRecord {
    @PrimaryKey(name = "id", type = "uuid")
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "comment", type = "text")
    private String comment;

    @Column(name = "rate", type = "int")
    private Integer rate;

    @OneToOne(refTable = "users", refColumn = "id", joinColumn = "user_id")
    private User user;

    @Column(name = "create_time", type = "datetime")
    private Date createdAt;

    @Column(name = "update_time", type = "datetime")
    private Date updatedAt;

    public User getUser() {
        return user;
    }
}

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
        User userModel = new User();
        Object[] values = {};

        User user = userModel.getFirst("users", "true", values, 3);

        for(Review t: user.getReviews()){
            System.out.println(t.getUser());
        }
    }
}