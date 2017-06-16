-- MySQL dump 10.13  Distrib 5.7.11, for osx10.11 (x86_64)
--
-- Host: 10.14.87.144    Database: haowu_base
-- ------------------------------------------------------
-- Server version	5.5.5-10.0.23-MariaDB-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `schedule_job`
--

DROP TABLE IF EXISTS `schedule_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `schedule_name` varchar(100) DEFAULT NULL COMMENT '任务名',
  `project` varchar(50) DEFAULT NULL COMMENT '归属项目',
  `class_name` varchar(500) DEFAULT NULL,
  `method_name` varchar(500) DEFAULT NULL,
  `time_expression` varchar(50) DEFAULT NULL COMMENT '执行时间表达式',
  `job_url` varchar(250) DEFAULT NULL COMMENT '任务远程地址(URL)',
  `job_type` varchar(50) DEFAULT NULL COMMENT '任务类型',
  `dubbo_version` varchar(50) DEFAULT NULL COMMENT 'dubbo版本',
  `execute_status` varchar(50) DEFAULT NULL COMMENT '执行状态',
  `param` varchar(200) DEFAULT NULL COMMENT '执行参数',
  `description` varchar(500) DEFAULT NULL,
  `delete_status` varchar(10) DEFAULT '0' COMMENT '0-未删除，1-已删除',
  `overtime` varchar(50) DEFAULT NULL COMMENT '超时时间',
  `notice_email` varchar(50) DEFAULT NULL COMMENT '错误通知地址(email)',
  `creater` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `modifier` varchar(100) DEFAULT NULL,
  `modify_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `prev_fire_time` datetime DEFAULT NULL,
  `next_fire_time` datetime DEFAULT NULL,
  `fire_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `schedule_name` (`schedule_name`,`project`,`time_expression`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8 COMMENT='定时任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schedule_job_execute_status`
--

DROP TABLE IF EXISTS `schedule_job_execute_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule_job_execute_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `jobId` bigint(20) NOT NULL COMMENT '任务ID',
  `jobKey` varchar(150) NOT NULL COMMENT '任务键值',
  `executeStatus` int(11) NOT NULL COMMENT '任务执行状态',
  `noticeAddress` varchar(50) DEFAULT NULL COMMENT '失败通知地址',
  `sendFlag` int(11) DEFAULT '0' COMMENT '发送标志, 0(未发送),1(已发送),2(不应该发送)',
  `createTime` datetime DEFAULT NULL,
  `modifyTime` datetime DEFAULT NULL,
  `trackingId` varchar(40) DEFAULT NULL COMMENT '追踪ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=732 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ScheduleJobLog`
--

DROP TABLE IF EXISTS `ScheduleJobLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ScheduleJobLog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `result` varchar(50) DEFAULT NULL,
  `creater` varchar(20) DEFAULT NULL,
  `modifier` varchar(20) DEFAULT NULL,
  `costTime` int(11) DEFAULT NULL,
  `createTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `executeLog` text,
  `jobKey` varchar(150) DEFAULT NULL,
  `lockKey` varchar(100) DEFAULT NULL,
  `modifyTime` datetime DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  `executeStage` varchar(30) DEFAULT NULL,
  `trackingId` varchar(40) DEFAULT NULL,
  `jobId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=132251 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schedule_notice_log`
--

DROP TABLE IF EXISTS `schedule_notice_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule_notice_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `jobKey` varchar(150) NOT NULL COMMENT '任务键值',
  `content` text COMMENT '发送内容',
  `createTime` datetime DEFAULT NULL,
  `overTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=241 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-11-08 18:34:50
