package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "heritage_work")
public class HeritageWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属匠人id，关联user.id */
    @Column(name = "craftsman_id")
    private Long craftsmanId;

    /** 作品标题 */
    private String title;

    /** 非遗分类，如剪纸、木雕、苏绣 */
    private String category;

    /** 封面图 */
    @Column(name = "cover_img")
    private String coverImg;

    /** 多张作品图片，逗号分隔存储url */
    @Column(name = "image_list")
    private String imageList;

    /** 技艺背景介绍 */
    @Column(name = "skill_background")
    private String skillBackground;

    /** 作品描述 */
    private String description;

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