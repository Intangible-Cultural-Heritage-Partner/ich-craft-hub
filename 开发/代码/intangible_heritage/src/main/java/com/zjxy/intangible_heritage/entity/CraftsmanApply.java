package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "craftsman_apply")
public class CraftsmanApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 申请人用户id */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 匠人简介与经历 (新增) */
    private String introduce;

    /** 擅长技艺介绍 */
    private String skillIntroduction;

    /** 佐证资料图片，多个以逗号分隔 */
    private String proofImages;

    /** 0待审核 1审核通过 2驳回 */
    private Integer auditStatus = 0;

    /** 审核备注/驳回理由 */
    private String auditRemark;

    /** 申请时间 */
    private LocalDateTime createTime = LocalDateTime.now();
}