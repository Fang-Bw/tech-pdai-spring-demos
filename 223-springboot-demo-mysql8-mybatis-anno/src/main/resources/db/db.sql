-- 数据库初始化脚本
-- 基于原始备份文件转换而来

-- 创建数据库（可选）
CREATE DATABASE IF NOT EXISTS java_orm_test DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE java_orm_test;

-- 角色表
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) NOT NULL,
                           `role_key` varchar(255) NOT NULL,
                           `description` varchar(255) DEFAULT NULL,
                           `create_time` datetime DEFAULT NULL,
                           `update_time` datetime DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- 插入角色数据
INSERT INTO `tb_role` VALUES (1,'admin','admin','admin','2021-09-08 17:09:15','2021-09-08 17:09:15');

-- 用户表
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `user_name` varchar(45) NOT NULL,
                           `password` varchar(45) NOT NULL,
                           `email` varchar(45) DEFAULT NULL,
                           `phone_number` int(11) DEFAULT NULL,
                           `description` varchar(255) DEFAULT NULL,
                           `create_time` datetime DEFAULT NULL,
                           `update_time` datetime DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- 插入用户数据
INSERT INTO `tb_user` VALUES (1,'pdai','dfasdf','suzhou.daipeng@gmail.com',1212121213,'afsdfsaf','2021-09-08 17:09:15','2021-09-08 17:09:15');

-- 用户角色关联表
DROP TABLE IF EXISTS `tb_user_role`;
CREATE TABLE `tb_user_role` (
                                `user_id` int(11) NOT NULL,
                                `role_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入用户角色关联数据
INSERT INTO `tb_user_role` VALUES (1,1);