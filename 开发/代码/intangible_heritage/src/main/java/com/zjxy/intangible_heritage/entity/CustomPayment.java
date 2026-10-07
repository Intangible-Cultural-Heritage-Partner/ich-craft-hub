package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 定制订单支付流水表
 * pay_type：1定金 2尾款
 * status：0待支付 1已支付 2已关闭
 */
@Entity
@Table(name = "custom_payment")
public class CustomPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联定制订单id */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 付款人（申请人用户）id */
    @Column(name = "payer_id", nullable = false)
    private Long payerId;

    /** 支付金额 */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 1定金 2尾款 */
    @Column(name = "pay_type", nullable = false)
    private Integer payType;

    /** 支付方式：MOCK / ALIPAY_SANDBOX */
    @Column(name = "pay_method", length = 20)
    private String payMethod;

    /** 商户订单号（唯一） */
    @Column(name = "out_trade_no", nullable = false, unique = true, length = 64)
    private String outTradeNo;

    /** 支付宝交易号 */
    @Column(name = "trade_no", length = 64)
    private String tradeNo;

    /** 0待支付 1已支付 2已关闭 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    public CustomPayment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getPayerId() { return payerId; }
    public void setPayerId(Long payerId) { this.payerId = payerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getPayType() { return payType; }
    public void setPayType(Integer payType) { this.payType = payType; }

    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    /** 类型文案 */
    public String getPayTypeText() {
        if (payType == null) return "";
        return payType == 1 ? "定金" : "尾款";
    }
}
