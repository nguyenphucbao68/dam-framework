package org.demo.models;

import org.dam.annotation.*;
import org.dam.mapper.ActiveRecord;

import java.util.Date;
import java.util.UUID;

@Table(name = "reviews")
public class Review extends ActiveRecord {
    @PrimaryKey(name = "id")
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "rate")
    private Integer rate;

    @OneToOne(refTable = "users", refColumn = "id", joinColumn = "user_id")
    private User user;

    @Column(name = "create_time")
    private Date createdAt;

    @Column(name = "update_time")
    private Date updatedAt;

    public User getUser() {
        return user;
    }
}
