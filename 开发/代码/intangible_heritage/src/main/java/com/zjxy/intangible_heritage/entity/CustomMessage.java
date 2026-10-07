package com.zjxy.intangible_heritage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 定制沟通消息表
 * 依附定制申请，只有 order_status=2 沟通中 允许新增
 */
@Entity
@Table(name = "custom_message")
public class CustomMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "custom_order_id", nullable = false)
    private Long customOrderId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "content")
    private String content;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "send_time", insertable = false, updatable = false)
    private LocalDateTime sendTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomOrderId() { return customOrderId; }
    public void setCustomOrderId(Long customOrderId) { this.customOrderId = customOrderId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
}
