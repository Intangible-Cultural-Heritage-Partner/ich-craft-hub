-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: intangible_heritage_platform
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collect`
--

DROP TABLE IF EXISTS `collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '收藏用户id',
  `target_type` tinyint NOT NULL COMMENT '1非遗展品heritage_work，2教程tutorial',
  `target_id` bigint NOT NULL COMMENT '被收藏对象id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`),
  CONSTRAINT `collect_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collect`
--

LOCK TABLES `collect` WRITE;
/*!40000 ALTER TABLE `collect` DISABLE KEYS */;
/*!40000 ALTER TABLE `collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '评论人id',
  `target_type` tinyint NOT NULL COMMENT '1非遗展品，2教程，3用户分享作品',
  `target_id` bigint NOT NULL COMMENT '被评论对象id',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `craftsman_apply`
--

DROP TABLE IF EXISTS `craftsman_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `craftsman_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申请人用户id',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `introduce` text NOT NULL COMMENT '匠人简介与经历',
  `skill_introduction` text NOT NULL COMMENT '擅长技艺介绍',
  `proof_images` text COMMENT '佐证资料图片，多个以逗号分隔',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注/驳回理由',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匠人申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `craftsman_apply`
--

LOCK TABLES `craftsman_apply` WRITE;
/*!40000 ALTER TABLE `craftsman_apply` DISABLE KEYS */;
INSERT INTO `craftsman_apply` VALUES (1,1,'徐锦泽','sdftgyhj','asedrftfy',NULL,1,NULL,'2026-10-07 12:35:37');
/*!40000 ALTER TABLE `craftsman_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `custom_message`
--

DROP TABLE IF EXISTS `custom_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `custom_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `custom_order_id` bigint NOT NULL COMMENT '所属定制申请id',
  `sender_id` bigint NOT NULL COMMENT '发送人id',
  `receiver_id` bigint NOT NULL COMMENT '接收人id',
  `content` text COMMENT '文本消息',
  `img_url` varchar(255) DEFAULT NULL COMMENT '消息附带图片',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `custom_order_id` (`custom_order_id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`),
  CONSTRAINT `custom_message_ibfk_1` FOREIGN KEY (`custom_order_id`) REFERENCES `custom_order` (`id`),
  CONSTRAINT `custom_message_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `custom_message_ibfk_3` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定制申请双向聊天消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `custom_message`
--

LOCK TABLES `custom_message` WRITE;
/*!40000 ALTER TABLE `custom_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `custom_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `custom_order`
--

DROP TABLE IF EXISTS `custom_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `custom_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_user_id` bigint NOT NULL COMMENT '发起申请用户id',
  `craftsman_id` bigint NOT NULL COMMENT '被申请匠人id',
  `work_desc` text NOT NULL COMMENT '定制作品描述',
  `material_require` text COMMENT '材质要求',
  `budget` varchar(100) DEFAULT NULL COMMENT '心理预算',
  `expect_finish_time` varchar(50) DEFAULT NULL COMMENT '期望完成时间',
  `ref_img` varchar(255) DEFAULT NULL COMMENT '参考图片',
  `remark` text COMMENT '备注留言',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '0新建申请 1已拒绝 2沟通中 3需求完结',
  `refuse_reason` varchar(300) DEFAULT NULL COMMENT '匠人拒绝理由',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `apply_user_id` (`apply_user_id`),
  KEY `craftsman_id` (`craftsman_id`),
  CONSTRAINT `custom_order_ibfk_1` FOREIGN KEY (`apply_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `custom_order_ibfk_2` FOREIGN KEY (`craftsman_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定制申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `custom_order`
--

LOCK TABLES `custom_order` WRITE;
/*!40000 ALTER TABLE `custom_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `custom_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (1,1,2,'tutorial','2026-10-08 22:59:53'),(2,3,1,'tutorial','2026-10-08 23:08:28'),(3,4,1,'tutorial','2026-10-09 00:16:05');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `heritage_work`
--

DROP TABLE IF EXISTS `heritage_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `heritage_work` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `craftsman_id` bigint NOT NULL COMMENT '所属匠人id，关联user.id',
  `title` varchar(100) NOT NULL COMMENT '作品标题',
  `category` varchar(50) NOT NULL COMMENT '非遗分类，如剪纸、木雕、苏绣',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面图',
  `image_list` text COMMENT '多张作品图片，逗号分隔存储url',
  `skill_background` text COMMENT '技艺背景介绍',
  `description` text COMMENT '作品描述',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '管理员驳回备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `craftsman_id` (`craftsman_id`),
  CONSTRAINT `heritage_work_ibfk_1` FOREIGN KEY (`craftsman_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匠人发布非遗展品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `heritage_work`
--

LOCK TABLES `heritage_work` WRITE;
/*!40000 ALTER TABLE `heritage_work` DISABLE KEYS */;
/*!40000 ALTER TABLE `heritage_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `like_record`
--

DROP TABLE IF EXISTS `like_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `like_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '点赞用户id',
  `target_type` tinyint NOT NULL COMMENT '1非遗展品，2教程，3用户分享作品',
  `target_id` bigint NOT NULL COMMENT '被点赞对象id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_like` (`user_id`,`target_type`,`target_id`),
  CONSTRAINT `like_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `like_record`
--

LOCK TABLES `like_record` WRITE;
/*!40000 ALTER TABLE `like_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `like_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tutorial`
--

DROP TABLE IF EXISTS `tutorial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutorial` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `craftsman_id` bigint NOT NULL COMMENT '发布匠人id，关联user.id',
  `title` varchar(100) NOT NULL COMMENT '教程标题',
  `description` text,
  `category` varchar(50) NOT NULL DEFAULT '其他',
  `tags` varchar(255) DEFAULT NULL,
  `cover_img` varchar(255) DEFAULT NULL COMMENT '教程封面',
  `content` text COMMENT '图文教程正文',
  `video_url` varchar(255) DEFAULT NULL COMMENT '教程视频地址，可以为空',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `content_images` text COMMENT '图文教程配图，多张图片URL逗号分隔',
  PRIMARY KEY (`id`),
  KEY `craftsman_id` (`craftsman_id`),
  CONSTRAINT `tutorial_ibfk_1` FOREIGN KEY (`craftsman_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手作教程';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tutorial`
--

LOCK TABLES `tutorial` WRITE;
/*!40000 ALTER TABLE `tutorial` DISABLE KEYS */;
INSERT INTO `tutorial` VALUES (1,1,'剪纸','sdrtyujk','剪纸','入门,亲子,节日','/uploads/tutorial/cover/72a223c7-de8c-4751-a2e1-21d956439457.jpg','asdfgh',NULL,1,NULL,'2026-10-07 12:37:50',NULL),(2,1,'中国传统刺绣','asxdcfghjk','刺绣','零基础,装饰','/uploads/tutorial/cover/c3e04574-dbd1-42bb-a080-c14295d0339e.jpg','第一步\r\n第二步',NULL,1,NULL,'2026-10-08 22:48:07','/uploads/tutorial/content/504789cd-8900-4e2f-ba42-7d11f61dfea3.jpeg,/uploads/tutorial/content/63e58ee6-aff5-4bb6-bf06-5f81f8f0f8b2.jpg');
/*!40000 ALTER TABLE `tutorial` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键id',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码（明文，待后续加密）',
  `phone` varchar(50) NOT NULL COMMENT '手机号',
  `role` tinyint DEFAULT '0' COMMENT '0普通用户 1匠人 2管理员',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像图片地址',
  `introduce` text COMMENT '匠人简介，普通用户可为空',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'xujinze','258066','11111111111',1,'/images/default_avatar.png','sdftgyhj','2026-10-07 10:59:31'),(2,'admin','admin123','13800000000',2,'/images/default_avatar.png',NULL,'2026-10-07 12:35:16'),(3,'xiaomi','258066','11111111112',0,'/images/default_avatar.png',NULL,'2026-10-08 23:08:08'),(4,'huawei','258066','11111111113',0,'/images/default_avatar.png',NULL,'2026-10-08 23:59:02');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_work`
--

DROP TABLE IF EXISTS `user_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_work` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发布普通用户id，关联user.id',
  `title` varchar(100) NOT NULL COMMENT '作品标题',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面图',
  `image_list` text COMMENT '多张图片url逗号分隔',
  `description` text COMMENT '作品描述',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1审核通过 2驳回',
  `audit_remark` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_work_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户分享手作作品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_work`
--

LOCK TABLES `user_work` WRITE;
/*!40000 ALTER TABLE `user_work` DISABLE KEYS */;
INSERT INTO `user_work` VALUES (1,3,'xiaojianzhi',NULL,'/uploads/userwork/images/41621234-6bea-4bef-b44d-5fc38c69b9cb.jpg','kanyu',1,NULL,'2026-10-08 23:57:25');
/*!40000 ALTER TABLE `user_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_work_comment`
--

DROP TABLE IF EXISTS `user_work_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_work_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `work_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户作品评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_work_comment`
--

LOCK TABLES `user_work_comment` WRITE;
/*!40000 ALTER TABLE `user_work_comment` DISABLE KEYS */;
INSERT INTO `user_work_comment` VALUES (1,1,3,'怎么样','2026-10-08 23:57:34'),(2,1,4,'可以呀','2026-10-08 23:59:38');
/*!40000 ALTER TABLE `user_work_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_work_like`
--

DROP TABLE IF EXISTS `user_work_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_work_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `work_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_work` (`user_id`,`work_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户作品点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_work_like`
--

LOCK TABLES `user_work_like` WRITE;
/*!40000 ALTER TABLE `user_work_like` DISABLE KEYS */;
INSERT INTO `user_work_like` VALUES (3,1,1),(1,3,1),(2,4,1);
/*!40000 ALTER TABLE `user_work_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'intangible_heritage_platform'
--

--
-- Dumping routines for database 'intangible_heritage_platform'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-09 12:52:06
