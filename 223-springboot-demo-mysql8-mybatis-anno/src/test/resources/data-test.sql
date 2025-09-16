-- 测试数据
-- 插入角色数据
INSERT INTO `tb_role` VALUES (1,'admin','admin','admin','2021-09-08 17:09:15','2021-09-08 17:09:15');
INSERT INTO `tb_role` VALUES (2,'user','user','普通用户','2021-09-08 17:09:15','2021-09-08 17:09:15');

-- 插入用户数据
INSERT INTO `tb_user` VALUES (1,'pdai','dfasdf','suzhou.daipeng@gmail.com',1212121213,'afsdfsaf','2021-09-08 17:09:15','2021-09-08 17:09:15');
INSERT INTO `tb_user` VALUES (2,'test','test123','test@example.com',1234567890,'测试用户','2021-09-08 17:09:15','2021-09-08 17:09:15');

-- 插入用户角色关联数据
INSERT INTO `tb_user_role` VALUES (1,1);
INSERT INTO `tb_user_role` VALUES (2,2);