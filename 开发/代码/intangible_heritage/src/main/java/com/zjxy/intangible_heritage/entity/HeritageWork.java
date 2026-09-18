package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "heritage_work")
public class HeritageWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "craftsman_id", nullable = false)
    private User craftsman;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String category = "其他";

    @Column(name = "cover_img", length = 255)
    private String coverImg;

    @Column(name = "image_list", columnDefinition = "TEXT")
    private String imageList;

    @Column(name = "skill_background", columnDefinition = "TEXT")
    private String skillBackground;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 0;

    @Column(name = "audit_remark", length = 200)
    private String auditRemark;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public HeritageWork() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCraftsman() {
        return craftsman;
    }

    public void setCraftsman(User craftsman) {
        this.craftsman = craftsman;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getImageList() {
        return imageList;
    }

    public void setImageList(String imageList) {
        this.imageList = imageList;
    }

    public String getSkillBackground() {
        return skillBackground;
    }

    public void setSkillBackground(String skillBackground) {
        this.skillBackground = skillBackground;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
