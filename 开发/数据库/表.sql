-- 创建数据库
CREATE DATABASE IF NOT EXISTS intangible_heritage_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE intangible_heritage_platform;

-- 1. comment 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '评论用户id',
    target_type TINYINT NOT NULL COMMENT '评论目标类型',
    target_id BIGINT NOT NULL COMMENT '目标id',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    parent_id BIGINT NULL DEFAULT NULL COMMENT '父评论id，用于回复',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- 2. craftsman_apply 匠人申请表
CREATE TABLE IF NOT EXISTS craftsman_apply (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '申请人用户id',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    introduce TEXT NOT NULL COMMENT '个人简介',
    skill_introduction TEXT NOT NULL COMMENT '技艺介绍',
    proof_images TEXT NULL DEFAULT NULL COMMENT '资质证明图片',
    audit_status TINYINT NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核',
    audit_remark VARCHAR(200) NULL DEFAULT NULL COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匠人申请表';

-- 3. custom_message 定制申请双向聊天消息表
CREATE TABLE IF NOT EXISTS custom_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    custom_order_id BIGINT NOT NULL COMMENT '定制订单id',
    sender_id BIGINT NOT NULL COMMENT '发送者id',
    receiver_id BIGINT NOT NULL COMMENT '接收者id',
    content TEXT NULL DEFAULT NULL COMMENT '消息文本',
    img_url VARCHAR(255) NULL DEFAULT NULL COMMENT '图片地址',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定制申请双向聊天消息';

-- 4. custom_order 定制申请表
CREATE TABLE IF NOT EXISTS custom_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    apply_user_id BIGINT NOT NULL COMMENT '申请用户id',
    craftsman_id BIGINT NOT NULL COMMENT '匠人id',
    work_desc TEXT NOT NULL COMMENT '作品需求描述',
    material_require TEXT NULL DEFAULT NULL COMMENT '材料要求',
    budget VARCHAR(100) NULL DEFAULT NULL COMMENT '预算',
    expect_finish_time VARCHAR(100) NULL DEFAULT NULL COMMENT '期望完成时间',
    ref_img VARCHAR(255) NULL DEFAULT NULL COMMENT '参考图片',
    remark TEXT NULL DEFAULT NULL COMMENT '备注',
    order_status TINYINT NOT NULL DEFAULT '0' COMMENT '订单状态 0新建申请',
    refuse_reason VARCHAR(300) NULL DEFAULT NULL COMMENT '拒绝理由',
    quote_price DECIMAL(10,2) NULL DEFAULT NULL COMMENT '报价总价',
    deposit_amount DECIMAL(10,2) NULL DEFAULT NULL COMMENT '定金金额',
    quote_note VARCHAR(300) NULL DEFAULT NULL COMMENT '报价备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定制申请表';

-- 5. custom_payment 定制订单支付流水表
CREATE TABLE IF NOT EXISTS custom_payment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '定制订单id',
    payer_id BIGINT NOT NULL COMMENT '付款人id',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_type TINYINT NOT NULL COMMENT '支付类型',
    pay_method VARCHAR(20) NULL DEFAULT NULL COMMENT '支付方式',
    out_trade_no VARCHAR(64) NOT NULL UNIQUE COMMENT '外部交易号',
    trade_no VARCHAR(64) NULL DEFAULT NULL COMMENT '平台内部流水号',
    status TINYINT NOT NULL DEFAULT '0' COMMENT '流水状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pay_time DATETIME NULL DEFAULT NULL COMMENT '实际支付时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定制订单支付流水';

-- 6. favorite 收藏表（第二张收藏表，target_type为varchar）
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户id',
    target_id BIGINT NOT NULL COMMENT '目标id',
    target_type VARCHAR(20) NOT NULL COMMENT '收藏目标类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

-- 7. heritage_work 匠人发布非遗展品表
CREATE TABLE IF NOT EXISTS heritage_work (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    craftsman_id BIGINT NOT NULL COMMENT '匠人id',
    title VARCHAR(100) NOT NULL COMMENT '展品标题',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    cover_img VARCHAR(255) NULL DEFAULT NULL COMMENT '封面图',
    image_list TEXT NULL DEFAULT NULL COMMENT '多图列表',
    skill_background TEXT NULL DEFAULT NULL COMMENT '技艺背景',
    description TEXT NULL DEFAULT NULL COMMENT '展品描述',
    audit_status TINYINT NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核',
    audit_remark VARCHAR(200) NULL DEFAULT NULL COMMENT '审核驳回备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    model_url VARCHAR(500) NULL DEFAULT NULL COMMENT '3D模型glb地址',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匠人发布非遗展品';

-- 8. like_record 点赞记录表
CREATE TABLE IF NOT EXISTS like_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户id',
    target_type TINYINT NOT NULL COMMENT '点赞目标类型',
    target_id BIGINT NOT NULL COMMENT '目标id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录表';

-- 9. tutorial 手作教程表
CREATE TABLE IF NOT EXISTS tutorial (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    craftsman_id BIGINT NOT NULL COMMENT '匠人id',
    title VARCHAR(100) NOT NULL COMMENT '教程标题',
    cover_img VARCHAR(255) NULL DEFAULT NULL COMMENT '封面图片',
    content TEXT NULL DEFAULT NULL COMMENT '图文内容',
    video_url VARCHAR(255) NULL DEFAULT NULL COMMENT '视频地址',
    audit_status TINYINT NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核',
    audit_remark VARCHAR(200) NULL DEFAULT NULL COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    description TEXT NULL DEFAULT NULL COMMENT '简介描述',
    category VARCHAR(100) NULL DEFAULT NULL COMMENT '分类',
    tags VARCHAR(500) NULL DEFAULT NULL COMMENT '标签，逗号分隔',
    content_images TEXT NULL DEFAULT NULL COMMENT '内容插图列表',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手作教程';

-- 10. 用户表 user
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `phone` VARCHAR(50) NOT NULL COMMENT '手机号',
  `role` TINYINT COMMENT '0普通用户 1匠人 2管理员',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `introduce` TEXT DEFAULT NULL COMMENT '个人简介',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 11. 用户作品表 user_work
CREATE TABLE `user_work` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户id',
  `title` VARCHAR(100) NOT NULL COMMENT '作品标题',
  `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `image_list` TEXT DEFAULT NULL COMMENT '作品图片列表',
  `description` TEXT DEFAULT NULL COMMENT '作品描述',
  `audit_status` TINYINT NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '审核备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `video_url` VARCHAR(255) DEFAULT NULL COMMENT '视频链接',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户分享手作作品';

-- 12. 用户作品评论表 user_work_comment
CREATE TABLE `user_work_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` BIGINT NOT NULL COMMENT '作品id',
  `user_id` BIGINT NOT NULL COMMENT '评论用户id',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户作品评论表';

-- 13. 用户作品点赞表 user_work_like
CREATE TABLE `user_work_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户id',
  `work_id` BIGINT NOT NULL COMMENT '被点赞作品id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_work` (`user_id`,`work_id`) -- 联合唯一，防止重复点赞
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户作品点赞表';

-- 14. 用户钱包账户 wallet_account
CREATE TABLE `wallet_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户id',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户钱包账户';

-- 15. 钱包交易流水 wallet_transaction
CREATE TABLE `wallet_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户id',
  `tx_type` VARCHAR(16) NOT NULL COMMENT '交易类型',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `balance_after` DECIMAL(10,2) DEFAULT NULL COMMENT '交易后余额',
  `status` TINYINT DEFAULT NULL COMMENT '交易状态',
  `ref_type` VARCHAR(20) DEFAULT NULL COMMENT '关联业务类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联业务id',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人id',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包交易流水';

-- 【可选】初始化管理员账号
INSERT INTO `user`(`username`,`password`,`phone`,`role`,`avatar`,`introduce`)
VALUES ('admin','123456','13800138000',2,'/images/default_avatar.png','平台超级管理员');
