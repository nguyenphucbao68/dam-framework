package org.example.models;

import org.example.annotation.*;
import org.example.mapper.ActiveRecord;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "users")
public class User extends ActiveRecord {
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

