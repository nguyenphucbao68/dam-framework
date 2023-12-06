package org.example.models;

import org.example.annotation.*;
import org.example.mapper.ActiveRecord;

import java.util.Date;
import java.util.UUID;

@Table(name = "reviews")
public class Review extends ActiveRecord {
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
