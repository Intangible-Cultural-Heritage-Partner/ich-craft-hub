-- ============================================================
-- 定制交易支付功能 增量脚本（只增不删，请勿重复执行 ALTER 部分）
-- 数据库：intangible_heritage_platform
-- ============================================================
USE intangible_heritage_platform;

-- 1. 定制订单表：新增报价相关字段
ALTER TABLE custom_order
    ADD COLUMN quote_price    DECIMAL(10,2) NULL COMMENT '匠人报价（总价）' AFTER refuse_reason,
    ADD COLUMN deposit_amount DECIMAL(10,2) NULL COMMENT '定金金额（报价×定金比例）' AFTER quote_price,
    ADD COLUMN quote_note     VARCHAR(300)  NULL COMMENT '报价说明' AFTER deposit_amount;

-- 2. 支付流水表
CREATE TABLE IF NOT EXISTS custom_payment (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水id',
    order_id     BIGINT       NOT NULL COMMENT '定制订单id',
    payer_id     BIGINT       NOT NULL COMMENT '付款人用户id（申请人）',
    amount       DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_type     TINYINT      NOT NULL COMMENT '1定金 2尾款',
    pay_method   VARCHAR(20)  NULL COMMENT '支付方式：MOCK/ALIPAY_SANDBOX',
    out_trade_no VARCHAR(64)  NOT NULL COMMENT '商户订单号（唯一）',
    trade_no     VARCHAR(64)  NULL COMMENT '支付宝交易号',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭',
    create_time  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pay_time     DATETIME     NULL COMMENT '支付时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_out_trade_no (out_trade_no),
    KEY idx_pay_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制订单支付流水';
