package org.example.models;

import org.example.annotation.*;
import org.example.mapper.ActiveRecord;

import java.util.Date;
import java.util.UUID;

@Table(name = "blogs")
public class Blog extends ActiveRecord {
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

