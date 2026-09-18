package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "heritage_work")
public class HeritageWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //所属匠人，关联 user.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "craftsman_id", nullable = false)
    private User craftsman;

    @Column(nullable = false, length = 100)
    private String title;

    //非遗分类，如剪纸、木雕、苏绣
    @Column(nullable = false, length = 50)
    private String category;

    //封面图
    @Column(name = "cover_img")
    private String coverImg;

    //多张作品图片，逗号分隔存储访问路径
    @Column(name = "image_list", columnDefinition = "TEXT")
    private String imageList;

    //3D模型文件访问路径（.glb/.gltf），可为空
    @Column(name = "model_url")
    private String modelUrl;

    //技艺背景介绍
    @Column(name = "skill_background", columnDefinition = "TEXT")
    private String skillBackground;

    //作品描述
    @Column(columnDefinition = "TEXT")
    private String description;

    //0待审核 1审核通过 2驳回
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 0;

    //管理员驳回备注
    @Column(name = "audit_remark", length = 200)
    private String auditRemark;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prepareCreateTime() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (auditStatus == null) {
            auditStatus = 0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCraftsman() { return craftsman; }
    public void setCraftsman(User craftsman) { this.craftsman = craftsman; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    public String getImageList() { return imageList; }
    public void setImageList(String imageList) { this.imageList = imageList; }

    public String getModelUrl() { return modelUrl; }
    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }

    public String getSkillBackground() { return skillBackground; }
    public void setSkillBackground(String skillBackground) { this.skillBackground = skillBackground; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
