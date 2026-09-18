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
    @Column(name = "user_id")
    private Long userId;

    /** 作品标题 */
    private String title;

    /** 封面图 */
    @Column(name = "cover_img")
    private String coverImg;

    /** 多张图片url，逗号分隔 */
    @Column(name = "image_list")
    private String imageList;

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