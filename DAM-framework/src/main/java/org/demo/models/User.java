package org.demo.models;

import org.dam.annotation.*;
import org.dam.mapper.ActiveRecord;

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

    public User(){}

    public User(
            String email,
            Integer role,
            String display_name,
            String avatarUrl,
            Date createdAt,
            Date updatedAt
    ) {
        this.email = email;
        this.role = role;
        this.display_name = display_name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

