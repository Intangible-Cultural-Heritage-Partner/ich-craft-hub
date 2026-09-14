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
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匠人申请表';