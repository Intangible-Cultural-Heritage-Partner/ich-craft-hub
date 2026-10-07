-- ============================================================
-- 钱包模块 增量脚本（只增不删）
-- 数据库：intangible_heritage_platform
-- ============================================================
USE intangible_heritage_platform;

-- 1. 钱包账户表（每个用户唯一账户）
CREATE TABLE IF NOT EXISTS wallet_account (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账户id',
    user_id     BIGINT       NOT NULL COMMENT '所属用户id',
    balance     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    update_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包账户';

-- 2. 钱包交易流水表
--    tx_type：RECHARGE 充值（需审核） / PAY_OUT 定制付款支出 / PAY_IN 定制收款
--    status（仅充值用）：0待审核 1审核通过 2已驳回；收支流水固定为 1
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水id',
    user_id      BIGINT       NOT NULL COMMENT '所属用户id',
    tx_type      VARCHAR(16)  NOT NULL COMMENT 'RECHARGE/PAY_OUT/PAY_IN',
    amount       DECIMAL(10,2) NOT NULL COMMENT '发生金额（恒为正数）',
    balance_after DECIMAL(10,2) NULL COMMENT '变动后余额',
    status       TINYINT      NULL COMMENT '充值审核状态：0待审核 1通过 2驳回；收支=1',
    ref_type     VARCHAR(20)  NULL COMMENT '关联业务类型，如 CUSTOM_PAY',
    ref_id       BIGINT       NULL COMMENT '关联业务id（支付流水id/订单id）',
    remark       VARCHAR(200) NULL COMMENT '备注/驳回理由',
    auditor_id   BIGINT       NULL COMMENT '审核管理员id',
    create_time  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    audit_time   DATETIME     NULL COMMENT '审核时间',
    PRIMARY KEY (id),
    KEY idx_wallet_user (user_id, create_time),
    KEY idx_recharge_audit (tx_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包交易流水';
