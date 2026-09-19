-- 创建数据库
CREATE DATABASE IF NOT EXISTS intangible_heritage_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE intangible_heritage_platform;

-- 1.用户表 user
-- 角色：USER普通用户 ｜ CRAFTSMAN匠人 ｜ ADMIN管理员
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键id',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（明文，待后续加密）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER普通用户 CRAFTSMAN匠人 ADMIN管理员',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像图片地址',
  `introduce` TEXT DEFAULT NULL COMMENT '匠人简介，普通用户可为空',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2.非遗展品表 heritage_work （匠人上传官方非遗展品）
DROP TABLE IF EXISTS `heritage_work`;
CREATE TABLE `heritage_work` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `craftsman_id` BIGINT NOT NULL COMMENT '所属匠人id，关联user.id',
  `title` VARCHAR(100) NOT NULL COMMENT '作品标题',
  `category` VARCHAR(50) NOT NULL COMMENT '非遗分类，如剪纸、木雕、苏绣',
  `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `image_list` TEXT DEFAULT NULL COMMENT '多张作品图片，逗号分隔存储url',
  `skill_background` TEXT DEFAULT NULL COMMENT '技艺背景介绍',
  `description` TEXT DEFAULT NULL COMMENT '作品描述',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '管理员驳回备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`craftsman_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匠人发布非遗展品';

-- 3.手作教程表 tutorial
DROP TABLE IF EXISTS `tutorial`;
CREATE TABLE `tutorial` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `craftsman_id` BIGINT NOT NULL COMMENT '发布匠人id，关联user.id',
  `title` VARCHAR(100) NOT NULL COMMENT '教程标题',
  `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '教程封面',
  `content` TEXT DEFAULT NULL COMMENT '图文教程正文',
  `video_url` VARCHAR(255) DEFAULT NULL COMMENT '教程视频地址，可以为空',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`craftsman_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手作教程';

-- 4.用户分享作品表 user_work（普通用户上传自己练习手作）
DROP TABLE IF EXISTS `user_work`;
CREATE TABLE `user_work` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '发布普通用户id，关联user.id',
  `title` VARCHAR(100) NOT NULL COMMENT '作品标题',
  `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `image_list` TEXT DEFAULT NULL COMMENT '多张图片url逗号分隔',
  `description` TEXT DEFAULT NULL COMMENT '作品描述',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通用户分享手作作品';

-- 5.收藏表 collect
-- 用户可以收藏非遗展品、教程
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '收藏用户id',
  `target_type` TINYINT NOT NULL COMMENT '1非遗展品heritage_work，2教程tutorial',
  `target_id` BIGINT NOT NULL COMMENT '被收藏对象id',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  UNIQUE KEY uk_user_target (`user_id`,`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 6.评论表 comment
-- 支持对非遗展品、教程、用户分享作品进行评论
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '评论人id',
  `target_type` TINYINT NOT NULL COMMENT '1非遗展品，2教程，3用户分享作品',
  `target_id` BIGINT NOT NULL COMMENT '被评论对象id',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 7.点赞表 like_record
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '点赞用户id',
  `target_type` TINYINT NOT NULL COMMENT '1非遗展品，2教程，3用户分享作品',
  `target_id` BIGINT NOT NULL COMMENT '被点赞对象id',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  UNIQUE KEY uk_user_like (`user_id`,`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 8.定制申请表 custom_order
-- 状态：0新建申请 1已拒绝 2沟通中 3需求完结
DROP TABLE IF EXISTS `custom_order`;
CREATE TABLE `custom_order` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `apply_user_id` BIGINT NOT NULL COMMENT '发起申请用户id',
  `craftsman_id` BIGINT NOT NULL COMMENT '被申请匠人id',
  `work_desc` TEXT NOT NULL COMMENT '定制作品描述',
  `material_require` TEXT DEFAULT NULL COMMENT '材质要求',
  `budget` VARCHAR(100) DEFAULT NULL COMMENT '心理预算',
  `expect_finish_time` VARCHAR(50) DEFAULT NULL COMMENT '期望完成时间',
  `ref_img` VARCHAR(255) DEFAULT NULL COMMENT '参考图片',
  `remark` TEXT DEFAULT NULL COMMENT '备注留言',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0新建申请 1已拒绝 2沟通中 3需求完结',
  `refuse_reason` VARCHAR(300) DEFAULT NULL COMMENT '匠人拒绝理由',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`apply_user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`craftsman_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制申请表';

-- 9.定制沟通消息表 custom_message
-- 依附定制申请，只有沟通中状态允许新增消息
DROP TABLE IF EXISTS `custom_message`;
CREATE TABLE `custom_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `custom_order_id` BIGINT NOT NULL COMMENT '所属定制申请id',
  `sender_id` BIGINT NOT NULL COMMENT '发送人id',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人id',
  `content` TEXT DEFAULT NULL COMMENT '文本消息',
  `img_url` VARCHAR(255) DEFAULT NULL COMMENT '消息附带图片',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`custom_order_id`) REFERENCES `custom_order`(`id`),
  FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制申请双向聊天消息';

-- 10.匠人申请表 craftsman_apply
USE `intangible_heritage_platform`;
DROP TABLE IF EXISTS `craftsman_apply`;
CREATE TABLE `craftsman_apply` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '申请人用户id',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `introduce` TEXT NOT NULL COMMENT '匠人简介与经历',
  `skill_introduction` TEXT NOT NULL COMMENT '擅长技艺介绍',
  `proof_images` TEXT DEFAULT NULL COMMENT '佐证资料图片，多个以逗号分隔',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '审核备注/驳回理由',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匠人申请表';
