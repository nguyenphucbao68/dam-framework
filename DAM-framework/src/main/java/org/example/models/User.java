package org.example.models;

import org.example.annotation.*;
import org.example.mapper.ActiveRecord;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "users")
public class User extends ActiveRecord {
    @PrimaryKey(name = "id")
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private Integer role = 0;

    @Column(name = "display_name")
    private String display_name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "create_time")
    private Date createdAt;

    @Column(name = "update_time")
    private Date updatedAt;

    @OneToMany(refTable = "reviews", refColumn = "user_id", joinColumn = "id")
    private List<Review> reviews;

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    // set display_name
    public void setDisplayName(String display_name) {
        this.display_name = display_name;
    }
}

