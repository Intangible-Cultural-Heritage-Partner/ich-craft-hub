package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_work_like", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "work_id"}))
public class UserWorkLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    public UserWorkLike() {}

    public UserWorkLike(Long userId, Long workId) {
        this.userId = userId;
        this.workId = workId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
}