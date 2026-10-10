package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易流水
 * tx_type：RECHARGE 充值（需审核） / PAY_OUT 定制付款支出 / PAY_IN 定制收款
 * status：充值 0待审核 1通过 2驳回；收支流水固定 1
 */
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** RECHARGE / PAY_OUT / PAY_IN */
    @Column(name = "tx_type", nullable = false, length = 16)
    private String txType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "status")
    private Integer status;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "auditor_id")
    private Long auditorId;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    public WalletTransaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTxType() { return txType; }
    public void setTxType(String txType) { this.txType = txType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Long getAuditorId() { return auditorId; }
    public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    /** 类型文案 */
    public String getTxTypeText() {
        if (txType == null) return "";
        return switch (txType) {
            case "RECHARGE" -> "充值";
            case "PAY_OUT" -> "定制支出";
            case "PAY_IN" -> "定制收入";
            default -> txType;
        };
    }

    /** 状态文案 */
    public String getStatusText() {
        if ("RECHARGE".equals(txType)) {
            if (status == null) return "";
            return switch (status) {
                case 0 -> "待审核";
                case 1 -> "已到账";
                case 2 -> "已驳回";
                default -> "";
            };
        }
        return "已完成";
    }
}
