package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_id", "target_type"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public Favorite() {}

    public Favorite(Long userId, Long targetId, String targetType) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}