package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tutorial")
public class Tutorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发布匠人id，关联user.id */
    @Column(name = "craftsman_id")
    private Long craftsmanId;

    /** 教程标题 */
    private String title;

    /** 教程封面 */
    @Column(name = "cover_img")
    private String coverImg;

    /** 教程简介 */
    private String description;

    /** 分类：剪纸/刺绣/陶艺/木雕/扎染/其他 */
    @Column(nullable = false, length = 50)
    private String category = "其他";

    /** 标签，逗号分隔，如 入门,窗花,传统 */
    @Column(length = 255)
    private String tags;

    /** 图文教程正文 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 教程视频地址，可以为空 */
    @Column(name = "video_url")
    private String videoUrl;

    /** 0待审核 1审核通过 2驳回 */
    @Column(name = "audit_status")
    private Integer auditStatus = 0;

    /** 管理员驳回备注 */
    @Column(name = "audit_remark")
    private String auditRemark;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}