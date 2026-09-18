package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 定制申请表
 * 状态：0新建申请 1已拒绝 2沟通中 3需求完结
 */
@Entity
@Table(name = "custom_order")
public class CustomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发起申请用户id */
    @Column(name = "apply_user_id", nullable = false)
    private Long applyUserId;

    /** 被申请匠人id */
    @Column(name = "craftsman_id", nullable = false)
    private Long craftsmanId;

    /** 定制作品描述 */
    @Column(name = "work_desc", nullable = false)
    private String workDesc;

    /** 材质要求 */
    @Column(name = "material_require")
    private String materialRequire;

    /** 心理预算 */
    @Column(name = "budget")
    private String budget;

    /** 期望完成时间 */
    @Column(name = "expect_finish_time")
    private String expectFinishTime;

    /** 参考图片url */
    @Column(name = "ref_img")
    private String refImg;

    /** 备注留言 */
    @Column(name = "remark")
    private String remark;

    /** 0新建 1已拒绝 2沟通中 3需求完结 */
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus = 0;

    /** 匠人拒绝理由 */
    @Column(name = "refuse_reason")
    private String refuseReason;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public CustomOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplyUserId() { return applyUserId; }
    public void setApplyUserId(Long applyUserId) { this.applyUserId = applyUserId; }

    public Long getCraftsmanId() { return craftsmanId; }
    public void setCraftsmanId(Long craftsmanId) { this.craftsmanId = craftsmanId; }

    public String getWorkDesc() { return workDesc; }
    public void setWorkDesc(String workDesc) { this.workDesc = workDesc; }

    public String getMaterialRequire() { return materialRequire; }
    public void setMaterialRequire(String materialRequire) { this.materialRequire = materialRequire; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public String getExpectFinishTime() { return expectFinishTime; }
    public void setExpectFinishTime(String expectFinishTime) { this.expectFinishTime = expectFinishTime; }

    public String getRefImg() { return refImg; }
    public void setRefImg(String refImg) { this.refImg = refImg; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }

    public String getRefuseReason() { return refuseReason; }
    public void setRefuseReason(String refuseReason) { this.refuseReason = refuseReason; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    /** 状态文案 */
    public String getStatusText() {
        if (orderStatus == null) return "";
        return switch (orderStatus) {
            case 0 -> "新建申请";
            case 1 -> "已拒绝";
            case 2 -> "沟通中";
            case 3 -> "需求完结";
            default -> "未知";
        };
    }
}
