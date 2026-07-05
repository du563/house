/*
 Navicat Premium Data Transfer

 Source Server         : gc
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : minsu

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 09/06/2026 11:11:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正文',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (1, '春节预订提示', '节假日房源紧张，建议提前预订并完成支付锁定房源。', 1, 0, '2026-04-19 13:27:50', '2026-04-19 13:27:50');
INSERT INTO `announcement` VALUES (2, '系统升级', '为提升体验，本平台已支持房源地图选点与位置展示。', 1, 10, '2026-04-19 13:27:50', '2026-04-19 13:27:50');
INSERT INTO `announcement` VALUES (4, '民宿房源紧张', '由于5月一日旅游旺季，导致民宿房源紧张。', 1, 5, '2026-05-15 17:08:27', '2026-05-15 17:08:27');

-- ----------------------------
-- Table structure for browse_history
-- ----------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL COMMENT '用户',
  `house_id` bigint NOT NULL COMMENT '房源',
  `browse_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '览时',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_browse_time`(`browse_time` ASC) USING BTREE,
  CONSTRAINT `浏览` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `看过` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 221 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '览史' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of browse_history
-- ----------------------------
INSERT INTO `browse_history` VALUES (49, 2, 5, '2026-04-17 20:48:51');
INSERT INTO `browse_history` VALUES (50, 2, 4, '2026-04-17 20:51:45');
INSERT INTO `browse_history` VALUES (51, 2, 9, '2026-04-17 20:54:23');
INSERT INTO `browse_history` VALUES (81, 2, 1, '2026-04-19 09:50:46');
INSERT INTO `browse_history` VALUES (84, 9, 4, '2026-04-19 15:25:14');
INSERT INTO `browse_history` VALUES (85, 9, 7, '2026-04-19 15:25:26');
INSERT INTO `browse_history` VALUES (86, 9, 7, '2026-04-19 15:28:15');
INSERT INTO `browse_history` VALUES (87, 2, 7, '2026-04-19 20:26:51');
INSERT INTO `browse_history` VALUES (88, 3, 7, '2026-04-19 20:28:53');
INSERT INTO `browse_history` VALUES (100, 2, 9, '2026-04-21 11:12:47');
INSERT INTO `browse_history` VALUES (123, 3, 7, '2026-05-14 17:28:25');
INSERT INTO `browse_history` VALUES (124, 3, 7, '2026-05-14 17:30:34');
INSERT INTO `browse_history` VALUES (137, 11, 7, '2026-05-15 10:41:01');
INSERT INTO `browse_history` VALUES (138, 11, 7, '2026-05-15 10:42:34');
INSERT INTO `browse_history` VALUES (139, 11, 7, '2026-05-15 10:43:40');
INSERT INTO `browse_history` VALUES (140, 11, 7, '2026-05-15 10:44:02');
INSERT INTO `browse_history` VALUES (141, 11, 7, '2026-05-15 10:47:02');
INSERT INTO `browse_history` VALUES (142, 11, 7, '2026-05-15 10:54:21');
INSERT INTO `browse_history` VALUES (143, 11, 7, '2026-05-15 10:54:33');
INSERT INTO `browse_history` VALUES (144, 11, 7, '2026-05-15 10:54:35');
INSERT INTO `browse_history` VALUES (145, 11, 7, '2026-05-15 10:56:42');
INSERT INTO `browse_history` VALUES (146, 11, 7, '2026-05-15 10:56:49');
INSERT INTO `browse_history` VALUES (194, 1, 3, '2026-06-01 10:08:37');
INSERT INTO `browse_history` VALUES (195, 1, 15, '2026-06-01 10:08:40');
INSERT INTO `browse_history` VALUES (196, 1, 9, '2026-06-01 10:29:31');
INSERT INTO `browse_history` VALUES (197, 1, 9, '2026-06-01 11:14:05');
INSERT INTO `browse_history` VALUES (198, 1, 15, '2026-06-01 11:14:13');
INSERT INTO `browse_history` VALUES (201, 12, 15, '2026-06-01 15:43:53');
INSERT INTO `browse_history` VALUES (202, 12, 9, '2026-06-01 15:43:58');
INSERT INTO `browse_history` VALUES (203, 12, 9, '2026-06-01 16:51:06');
INSERT INTO `browse_history` VALUES (204, 12, 7, '2026-06-01 16:53:26');
INSERT INTO `browse_history` VALUES (205, 11, 4, '2026-06-01 17:02:59');
INSERT INTO `browse_history` VALUES (206, 12, 4, '2026-06-01 17:08:34');
INSERT INTO `browse_history` VALUES (220, 4, 3, '2026-06-01 22:36:06');
INSERT INTO `browse_history` VALUES (221, 11, 9, '2026-06-02 07:55:34');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL COMMENT '用户',
  `house_id` bigint NOT NULL COMMENT '房源',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文',
  `score` int NOT NULL COMMENT '评分',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附图',
  `audit_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '审态',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审注',
  `reply_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回文',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回时',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `回复`(`reply_user_id` ASC) USING BTREE,
  CONSTRAINT `关联` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `回复` FOREIGN KEY (`reply_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `评价` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `针对` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 2, 9, 2, '还不错，环境优美！', 5, NULL, 1, '', '感谢顾客支持！', '2026-06-01 22:18:52', 4, '2026-04-17 20:54:08');
INSERT INTO `comment` VALUES (2, 9, 7, 5, '房间非常干净整洁，是一个旅游的不二之选。', 5, NULL, 1, '', NULL, NULL, NULL, '2026-04-19 15:28:09');
INSERT INTO `comment` VALUES (3, 12, 7, 8, '这个湖畔度假别墅住起来舒适感十足，这是一个非常适合假期带领家人游玩住宿的地方。', 5, NULL, 1, '', '感谢住户对我家民宿的喜欢！', '2026-05-15 11:17:45', 5, '2026-05-15 10:39:02');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL COMMENT '用户',
  `house_id` bigint NOT NULL COMMENT '房源',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_house`(`user_id` ASC, `house_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  CONSTRAINT `指向` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `收藏` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (4, 5, 10, '2026-05-15 15:28:16', '2026-05-15 15:28:16');

-- ----------------------------
-- Table structure for house
-- ----------------------------
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '户型',
  `capacity` int NULL DEFAULT 2 COMMENT '人数',
  `score` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `score_count` int NULL DEFAULT 0 COMMENT '评数',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `facilities` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施',
  `tag_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标串',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `merchant_id` bigint NULL DEFAULT NULL COMMENT '商户',
  `audit_status` tinyint(1) NULL DEFAULT 1 COMMENT '审态',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审注',
  `stock` int NULL DEFAULT 1 COMMENT '库存',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_area`(`area` ASC) USING BTREE,
  INDEX `idx_price`(`price` ASC) USING BTREE,
  INDEX `idx_score`(`score` ASC) USING BTREE,
  INDEX `归属`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `归属` FOREIGN KEY (`merchant_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房源' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house
-- ----------------------------
INSERT INTO `house` VALUES (1, '温馨海景房', '房间直面壮阔海景，清晨可在床上迎接阳光，傍晚欣赏海上日落。装修温馨舒适，设施齐全，周边步行可达海边沙滩、海鲜餐厅与网红打卡点，环境安静惬意，适合情侣度假、家庭出行，体验理想的海边生活。', 299.00, '青岛', '青岛市市南区海滨路100号', NULL, NULL, '一室一厅', 2, 4.8, 0, 'http://localhost:9000/upload/1774350125296_温馨海景房.jpg,http://localhost:9000/upload/1775723514651_温馨海景房.jpeg,http://localhost:9000/upload/1775725902093_温馨海景房.jpg', 'WiFi,空调,热水器,电视', '1,5', 1, 3, 1, '房源优质，予以通过', 3, '2026-03-18 15:33:18', '2026-05-14 18:03:18');
INSERT INTO `house` VALUES (2, '城市中心公寓', '位于城市核心商圈，地铁口步行即达，出行高效便捷。房间空间宽敞明亮，装修简约大气，生活设备齐全，满足长住短租需求。周边商场、超市、餐饮密集，是商务出差、城市旅游、工作通勤的便利之选。', 199.00, '北京', '北京市朝阳区建国路88号', NULL, NULL, '两室一厅', 4, 4.5, 0, 'http://localhost:9000/upload/1774350246376_城市中心公寓.png,http://localhost:9000/upload/1775725998678_城市中心公寓.jpeg,http://localhost:9000/upload/1775726024291_城市中心公寓.jpeg', 'WiFi,空调,洗衣机,厨房', '2', 1, 3, 1, '房源优质，予以通过', 2, '2026-03-18 15:33:18', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (3, '山间小屋', '隐匿于青山绿树之间，远离城市喧嚣，独享自然宁静。小屋风格简约质朴，开窗即是绿意与新鲜空气，周边适合散步登山。环境安静清幽，适合喜欢自然、追求放松的客人，享受山林间的慢节奏生活。', 159.00, '杭州', '杭州市西湖区灵隐路50号', NULL, NULL, '一室', 2, 4.9, 0, 'http://localhost:9000/upload/1775726871282_温馨小屋.jpg,http://localhost:9000/upload/1775726885612_温馨小屋.jpg,http://localhost:9000/upload/1775726899657_温馨小屋.jpeg', 'WiFi,空调,停车位', '3', 1, 5, 1, '房源优质，予以通过', 1, '2026-03-18 15:33:18', '2026-05-29 10:39:55');
INSERT INTO `house` VALUES (4, '豪华江景套房', '占据城市黄金江景位置，落地窗俯瞰壮阔江景与城市夜景。装修奢华大气，空间开阔通透，高端家具家电齐全，适合家庭出游、朋友聚会、商务接待。小区环境优质，周边配套完善，带来尊享品质的旅居体验。', 599.00, '上海', '上海市浦东新区滨江大道200号', NULL, NULL, '三室两厅', 6, 4.7, 0, 'http://localhost:9000/upload/1774350423881_豪华江景套房.jpg,http://localhost:9000/upload/1775726094301_豪华江景套房.jpeg,http://localhost:9000/upload/1775726110812_豪华江景套房.jpeg', 'WiFi,空调,热水器,电视,洗衣机,厨房', '1,4', 1, 4, 1, '房源优质，予以通过', 2, '2026-03-18 15:33:18', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (5, '古镇民居', '保留传统江南民居风格，白墙黛瓦充满古朴韵味，位于古镇核心区域。步行可体验小桥流水、青石板路、特色小吃。房间整洁舒适，兼具传统与现代风格，适合喜欢历史文化、拍照打卡、慢生活的客人。', 129.00, '苏州', '苏州市姑苏区平江路10号', NULL, NULL, '一室一厅', 2, 4.6, 0, 'http://localhost:9000/upload/1774350506323_古镇民居.jpg,http://localhost:9000/upload/1775726196543_古镇民居.jpeg,http://localhost:9000/upload/1775726221590_古镇民居.jpeg', 'WiFi,空调,热水器', '3,5', 1, 5, 1, '房源优质，予以通过', 4, '2026-03-18 15:33:18', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (6, '现代简约风公寓', '采用极简现代设计，干净清爽，空间利用率高。房间采光通风良好，配备高品质床品与生活设施，满足日常旅居需求。小区环境整洁安静，交通便利，适合追求简约舒适、高性价比的年轻客人与商务人士。', 249.00, '深圳', '深圳市南山区科技园路66号', NULL, NULL, '两室一厅', 3, 4.4, 0, 'http://localhost:9000/upload/1774350595636_现代简约风公寓.jpeg,http://localhost:9000/upload/1775726304975_现代简约风公寓.jpeg,http://localhost:9000/upload/1775726319293_现代简约风公寓.jpeg', 'WiFi,空调,洗衣机,健身房', '2,6', 1, 3, 1, '房源优质，予以通过', 3, '2026-03-18 15:33:18', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (7, '湖畔度假别墅', '临湖而建，独享一线湖景与私密花园，环境清幽视野开阔。别墅空间超大，多室多厅布局，可容纳多人家庭聚会、团队团建。设施齐全私密性强，周边自然环境优美，适合度假放松、休闲娱乐。', 899.00, '杭州', '杭州市西湖区杨公堤18号', NULL, NULL, '四室三厅', 8, 5.0, 3, 'http://localhost:9000/upload/1774350645298_湖畔度假别墅.jpg,http://localhost:9000/upload/1775726373708_湖畔度假别墅.jpg,http://localhost:9000/upload/1775726392142_湖畔度假别墅.jpg', 'WiFi,空调,停车位,花园,泳池', '4,5', 1, 5, 1, '房源优质，予以通过', 10, '2026-03-18 15:33:18', '2026-05-15 10:22:19');
INSERT INTO `house` VALUES (8, '文艺青年旅舍', '专为年轻人打造的文艺住宿空间，装修清新文艺，公共区域宽敞舒适。适合看书聊天交友，认识各地旅行伙伴。床位整洁，性价比高，周边美食景点密集，适合学生、背包客、独行旅行者。', 89.00, '成都', '成都市锦江区春熙路128号', NULL, NULL, '单间', 1, 4.3, 0, 'http://localhost:9000/upload/1774350697429_文艺青年旅舍.jpeg,http://localhost:9000/upload/1775726437997_文艺青年旅舍.jpeg,http://localhost:9000/upload/1775726455656_文艺青年旅舍.jpg', 'WiFi,空调,公共厨房', '2,3', 1, 5, 1, '', 10, '2026-03-18 15:33:18', '2026-06-01 22:15:32');
INSERT INTO `house` VALUES (9, '山崖旅社', '坐落于山崖观景地带，视野开阔，白天俯瞰山林远景，夜晚仰望星空灯火。房间设计舒适，设施齐全干净整洁，适合喜欢独特风景、追求新奇体验、热爱自然与摄影的客人，感受山崖之间的独特美景。', 300.00, '杭州', '杭州市西湖区杨公堤18号', NULL, NULL, '一室一厅', 2, 5.0, 2, 'http://localhost:9000/upload/1774350749830_山崖旅社.jpg,http://localhost:9000/upload/1775726519656_山崖旅社.jpeg,http://localhost:9000/upload/1775726533046_山崖旅社.jpg', 'WiFi,空调,热水器,电视,洗衣机,厨房', '3,4', 1, 4, 1, '房源优质，予以通过', 1, '2026-03-24 16:29:46', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (10, '雨林民宿', '环境充满自然雨林气息，绿植环绕空气清新，如同住进天然氧吧。房间装修温馨自然，设施齐全舒适，适合放松身心度假休闲。民宿位置便利，靠近景区与交通节点，出行游玩方便，是家庭出游、情侣度假的理想选择。', 500.00, '深圳', '深圳市罗湖区南湖街道罗湖桥社区建设路1003号', NULL, NULL, '两室一厅', 2, 5.0, 0, 'http://localhost:9000/upload/1774350990659_雨林民宿.jpg,http://localhost:9000/upload/1775726581053_雨林民宿.jpeg,http://localhost:9000/upload/1775726591546_雨林民宿.jpeg', 'WiFi,空调,热水器,电视,洗衣机,厨房', '5', 1, 4, 1, '房源优质，予以通过', 5, '2026-03-24 19:17:11', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (14, '南京别墅', '坐落于南京梧桐大道旁，道路两旁梧桐成荫，四季风景如画，氛围浪漫宁静。别墅空间宽敞大气，装修精致舒适，自带花园停车位，适合家庭出行、朋友聚会、情侣度假，步行即可感受南京老城韵味。', 300.00, '南京', '江苏省南京市江宁区淳化街道梧桐路南京传媒学院', 118.892300, 31.887577, '一室一厅', 2, 5.0, 0, 'http://localhost:9000/upload/1776481720385_梧桐大道.png,http://localhost:9000/upload/1776481747115_梧桐大道.jpeg,http://localhost:9000/upload/1776481764523_梧桐大道.jpeg', 'WiFi,空调,停车位,花园,泳池', '4', 1, NULL, 1, '', 1, '2026-04-18 11:09:38', '2026-04-24 17:11:50');
INSERT INTO `house` VALUES (15, 'aaa', '无', 300.00, '南京', '江苏省南京市玄武区玄武门街道南京市人民政府', 118.796624, 32.059344, '两室一厅', 2, 5.0, 0, 'http://localhost:9000/upload/1778753667968_李白.jpeg', 'WiFi,空调,洗衣机,健身房', '4,2', 1, 3, 1, '', 10, '2026-05-14 18:14:40', '2026-05-14 19:45:18');
INSERT INTO `house` VALUES (16, 'bbb', '无', 300.00, '东城区', '北京市东城区东华门街道天安门', 116.397428, 39.909230, '一室一厅', 3, 5.0, 0, 'http://localhost:9000/upload/1780305482131_小猫.jpeg', '空调', '2,3,4', 1, 4, 1, '', 10, '2026-06-01 17:18:06', '2026-06-01 22:16:19');

-- ----------------------------
-- Table structure for house_tag
-- ----------------------------
DROP TABLE IF EXISTS `house_tag`;
CREATE TABLE `house_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房标' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_tag
-- ----------------------------
INSERT INTO `house_tag` VALUES (1, '海景房', 1, 0, '2026-04-21 11:34:52', '2026-04-21 11:34:52');
INSERT INTO `house_tag` VALUES (2, '亲子房', 1, 10, '2026-04-21 11:34:52', '2026-04-21 11:34:52');
INSERT INTO `house_tag` VALUES (3, '田园风格', 1, 20, '2026-04-21 11:34:52', '2026-04-21 11:34:52');
INSERT INTO `house_tag` VALUES (4, '商务出行', 1, 30, '2026-04-21 11:34:52', '2026-04-21 11:34:52');
INSERT INTO `house_tag` VALUES (5, '情侣约会', 1, 40, '2026-04-21 11:34:52', '2026-04-21 11:34:52');
INSERT INTO `house_tag` VALUES (6, '宠物友好', 1, 50, '2026-04-21 11:34:52', '2026-04-21 11:34:52');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单号',
  `user_id` bigint NOT NULL COMMENT '用户',
  `house_id` bigint NOT NULL COMMENT '房源',
  `house_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '房名',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `total_price` decimal(10, 2) NOT NULL COMMENT '总价',
  `check_in_date` date NOT NULL COMMENT '入日',
  `check_out_date` date NOT NULL COMMENT '离日',
  `days` int NOT NULL COMMENT '天数',
  `guests` int NULL DEFAULT 1 COMMENT '客数',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联称',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联话',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态',
  `check_in_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '确入',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '入时',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `下单` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `预订` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, '98962C3132A64F6FB4EA', 2, 7, '湖畔度假别墅', 899.00, 1798.00, '2026-03-26', '2026-03-28', 2, 1, '我跟个', '15236597456', 3, 1, NULL, '无', '2026-03-18 17:06:17', '2026-04-20 16:41:16');
INSERT INTO `orders` VALUES (2, '498757D6AA8A4DF5AC69', 2, 9, '山崖旅社', 300.00, 900.00, '2026-04-25', '2026-04-28', 3, 1, '李四', '13696332693', 2, 1, NULL, '无', '2026-04-17 20:43:12', '2026-04-20 16:41:18');
INSERT INTO `orders` VALUES (3, '1E6156DEB1F644388A17', 2, 5, '古镇民居', 129.00, 258.00, '2026-05-01', '2026-05-03', 2, 1, '李四', '13698763213', 2, 1, NULL, '无', '2026-04-17 20:49:21', '2026-04-20 16:41:18');
INSERT INTO `orders` VALUES (4, 'B49F75480D244AE7AB67', 2, 4, '豪华江景套房', 599.00, 1198.00, '2026-04-28', '2026-04-30', 2, 5, '王五', '13756986321', 2, 1, NULL, '无', '2026-04-17 20:52:07', '2026-04-20 16:41:19');
INSERT INTO `orders` VALUES (5, 'E896090298ED4027B38B', 9, 7, '湖畔度假别墅', 899.00, 1798.00, '2026-05-01', '2026-05-03', 2, 3, '张三', '13698636213', 2, 1, NULL, '无', '2026-04-19 15:25:55', '2026-04-20 16:41:19');
INSERT INTO `orders` VALUES (6, '0F028C42A7FD45E49018', 2, 7, '湖畔度假别墅', 899.00, 1798.00, '2026-04-20', '2026-04-22', 2, 5, 'aaa', '13789631236', 2, 1, NULL, '无', '2026-04-19 20:27:31', '2026-04-20 16:41:22');
INSERT INTO `orders` VALUES (7, '8A77DB554A5045D5A6AE', 2, 9, '山崖旅社', 300.00, 600.00, '2026-04-23', '2026-04-25', 2, 1, '李白', '13698636212', 2, 1, '2026-04-21 11:14:32', '无', '2026-04-21 11:13:24', '2026-04-21 11:14:32');
INSERT INTO `orders` VALUES (8, 'CE4B0B318BB844AE8C0C', 12, 7, '湖畔度假别墅', 899.00, 899.00, '2026-05-15', '2026-05-16', 1, 3, '王五', '13696321362', 2, 1, '2026-05-14 20:25:49', '无', '2026-05-14 19:57:10', '2026-05-14 20:25:49');
INSERT INTO `orders` VALUES (9, '453D8BE294B04B66A80B', 12, 9, '山崖旅社', 300.00, 1500.00, '2026-05-15', '2026-05-20', 5, 1, '王五', '13698612362', 3, 0, NULL, '无', '2026-05-14 20:10:19', '2026-05-14 20:10:19');
INSERT INTO `orders` VALUES (10, '3EE8F58BF35A4692A276', 12, 7, '湖畔度假别墅', 899.00, 2697.00, '2026-06-02', '2026-06-05', 3, 3, 'aaa', '13656396863', 1, 0, NULL, '无', '2026-06-01 16:54:45', '2026-06-01 16:54:45');
INSERT INTO `orders` VALUES (11, 'D1DB600BD3AC428DB9FE', 11, 4, '豪华江景套房', 599.00, 1797.00, '2026-06-02', '2026-06-05', 3, 3, 'aaa', '15632689632', 2, 1, '2026-06-01 17:04:30', 'wu', '2026-06-01 17:03:44', '2026-06-01 17:04:30');
INSERT INTO `orders` VALUES (12, 'A2146B40F8C84293B696', 12, 4, '豪华江景套房', 599.00, 2396.00, '2026-06-01', '2026-06-05', 4, 3, 'aaa', '13689656321', 2, 1, '2026-06-01 17:10:12', 'wu', '2026-06-01 17:09:19', '2026-06-01 17:10:12');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL COMMENT '作者',
  `topic_id` bigint NOT NULL COMMENT '话题',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附图',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审注',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '赞数',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '览数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_topic_status_time`(`topic_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `发布` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `归类` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, 2, 1, '周末在古城民宿住了两晚，体验感超出预期', '这家民宿离古城步行约8分钟，晚上很安静。房间卫生不错，床品干净，店家还给了周边小吃地图。建议选择高楼层，视野更好。', NULL, 1, NULL, 1, 36, '2026-04-16 11:01:02', '2026-04-22 11:01:03');
INSERT INTO `post` VALUES (2, 9, 3, '入住避坑：下单前一定确认停车位', '很多热门地段停车紧张，建议下单前与房东确认是否有免费停车位、距离多远、夜间是否收费，避免临时找车位影响体验。', NULL, 1, NULL, 1, 29, '2026-04-17 11:01:03', '2026-04-22 11:01:03');
INSERT INTO `post` VALUES (3, 2, 2, '民宿早餐是否值得加购？我的真实对比', '如果是带娃出行，早餐加购通常省心；如果喜欢在外面打卡本地早餐店，可以不加购。关键看你们每天出发时间和行程强度。', NULL, 1, NULL, 1, 22, '2026-04-18 11:01:03', '2026-04-22 11:01:03');
INSERT INTO `post` VALUES (4, 9, 4, '拍照机位分享：露台日落真的绝', '建议傍晚提前20分钟上露台，占靠栏杆位置。手机开2x拍人像很出片，阴天也可以拍氛围感，记得带个便携补光灯。', NULL, 1, NULL, 1, 18, '2026-04-19 11:01:03', '2026-04-22 11:01:03');
INSERT INTO `post` VALUES (5, 2, 2, '发票和押金问题，前台怎么沟通更高效？', '建议入住时先确认押金金额、退还时点、开票主体和开票类型。把需求一次性说清楚，退房时会顺畅很多。', NULL, 1, NULL, 1, 14, '2026-04-20 11:01:03', '2026-05-15 15:54:15');
INSERT INTO `post` VALUES (6, 12, 4, '江边晚风氛围感拉满', '建议日落前半小时到江边，抢占临水步道机位。手机用 1.5x 拍半身超有质感，晚风配落日余晖自带滤镜，傍晚光线暗可以带个小反光板，随手都是大片。', NULL, 1, '', 1, 0, '2026-05-15 16:00:09', '2026-05-15 16:09:11');
INSERT INTO `post` VALUES (7, 12, 1, 'wu', '1234556', NULL, 1, '', 0, 0, '2026-06-01 17:15:30', '2026-06-01 17:15:59');

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `post_id` bigint NOT NULL COMMENT '帖子',
  `user_id` bigint NOT NULL COMMENT '用户',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `点于` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `点赞` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '赞记' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_like
-- ----------------------------
INSERT INTO `post_like` VALUES (1, 1, 9, '2026-04-21 11:01:03', '2026-04-21 11:01:03');
INSERT INTO `post_like` VALUES (2, 2, 2, '2026-04-21 11:01:03', '2026-04-21 11:01:03');
INSERT INTO `post_like` VALUES (3, 3, 9, '2026-04-21 15:01:03', '2026-04-21 15:01:03');
INSERT INTO `post_like` VALUES (4, 4, 2, '2026-04-21 20:01:03', '2026-04-21 20:01:03');
INSERT INTO `post_like` VALUES (5, 5, 9, '2026-04-22 03:01:03', '2026-04-22 03:01:03');
INSERT INTO `post_like` VALUES (7, 6, 1, '2026-05-15 16:09:11', '2026-05-15 16:09:11');

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of topic
-- ----------------------------
INSERT INTO `topic` VALUES (1, '旅行见闻', '分享你在民宿与旅途中的有趣经历', 1, 0, '2026-04-22 10:49:13', '2026-05-15 15:37:44');
INSERT INTO `topic` VALUES (2, '入住体验', '评价服务、卫生、交通等入住感受', 1, 10, '2026-04-22 10:49:13', '2026-04-22 10:49:13');
INSERT INTO `topic` VALUES (3, '避坑指南', '交流预订、入住时的注意事项', 1, 20, '2026-04-22 10:49:13', '2026-04-22 10:49:13');
INSERT INTO `topic` VALUES (4, '拍照打卡', '晒民宿与周边美景美图', 1, 30, '2026-04-22 10:49:13', '2026-04-22 10:49:13');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `role` tinyint(1) NULL DEFAULT 0 COMMENT '角色',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建时',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '改时',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$Rr/3uGd0CXDgT36LwsJxl.9bDMGpjmUoNBE7T/WECmMyHBlT9ONSy', '13800000000', NULL, 'http://localhost:9000/upload/user/1776429054594_管理员.jpeg', 1, 1, '2026-03-18 15:33:18', '2026-04-17 20:30:56');
INSERT INTO `user` VALUES (2, 'test', '$2a$10$BvdhDyFLsh0YmeO72sDQA.urVct8shJGn4BD9X1nPNYAt/Y4FtY02', '19756936982', NULL, 'http://localhost:9000/upload/user/1776429112310_用户1.jpeg', 0, 1, '2026-04-16 17:24:26', '2026-04-16 17:38:39');
INSERT INTO `user` VALUES (3, 'shanghu1', '$2a$10$XnAwZ4XhFxjGzzbvvhryvOg77rpBKhczAcrmLE00OYFFBFcxEs16.', '15263986352', NULL, 'http://localhost:9000/upload/user/1776429355546_商户2.jpeg', 2, 1, '2026-04-16 17:25:29', '2026-04-16 17:38:41');
INSERT INTO `user` VALUES (4, 'shanghu2', '$2a$10$ppKqCJ75twasPhT3AeP2bebYtIyKMUUvmVQ3fWYTtg58miVLLEwT2', '13969863265', NULL, 'http://localhost:9000/upload/user/1776429441062_商户1.jpeg', 2, 1, '2026-04-16 17:38:07', '2026-04-16 17:38:43');
INSERT INTO `user` VALUES (5, 'shanghu3', '$2a$10$JDSdloFHxuceX3DK.Uxz/eYRnopuDFFSwDgHWJF7x3rCUXageWXwa', '15763986321', NULL, 'http://localhost:9000/upload/user/1776429398937_商户3.jpeg', 2, 1, '2026-04-16 17:38:30', '2026-04-16 17:38:45');
INSERT INTO `user` VALUES (9, 'zhangsan', '$2a$10$W.pFfYiDJxWKA71lvE6Wi.tPb.9MbW.PD.kKzFClDi3oK0gkow8gm', '13269863261', NULL, 'http://localhost:9000/upload/user/1776583468163_用户2.jpeg', 0, 1, '2026-04-19 15:22:42', '2026-04-19 15:24:29');
INSERT INTO `user` VALUES (10, 'wangwu', '$2a$10$u7fFIgqp6Llvb4DuP2usxuOfUV87R0hvwUmd4ZTtFwZiENYz40pLa', '15693662312', '2052450165@qq.com', NULL, 0, 1, '2026-04-24 16:24:58', '2026-04-24 16:24:58');
INSERT INTO `user` VALUES (11, 'libai', '$2a$10$Jg91chZg8lYVSXKkWPIrR.6VH5Fzu.vjXqLqx4Q0NSMfkESqBMD7O', '13696321632', 'gc123m@126.com', 'http://localhost:9000/upload/user/1778662914629_李白.jpeg', 0, 1, '2026-04-24 16:30:49', '2026-04-24 16:30:49');
INSERT INTO `user` VALUES (12, 'baiqi', '$2a$10$bGaHAUQtrhAYSnTQmLq9XO1zRGhPj3IqFS5bra4ISfpSuWLIp3J8.', '13698763212', '2052450165@qq.com', '', 0, 1, '2026-05-13 17:25:22', '2026-05-14 16:05:19');

SET FOREIGN_KEY_CHECKS = 1;
