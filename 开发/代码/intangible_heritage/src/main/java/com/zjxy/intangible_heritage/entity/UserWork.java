package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_work")
public class UserWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发布普通用户id，关联user.id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 作品标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 封面图 */
    @Column(name = "cover_img")
    private String coverImg;

    /** 多张图片url，逗号分隔 */
    @Column(name = "image_list", columnDefinition = "TEXT")
    private String imageList;

    /** 作品描述 */
    @Column(name = "video_url")
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 0待审核 1审核通过 2驳回 */
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 0;

    /** 管理员驳回备注 */
    @Column(name = "audit_remark")
    private String auditRemark;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    public String getImageList() { return imageList; }
    public void setImageList(String imageList) { this.imageList = imageList; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}