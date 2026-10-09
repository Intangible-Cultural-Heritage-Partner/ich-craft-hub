package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_work_comment")
public class UserWorkComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public UserWorkComment() {}

    public UserWorkComment(Long workId, Long userId, String content) {
        this.workId = workId;
        this.userId = userId;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}