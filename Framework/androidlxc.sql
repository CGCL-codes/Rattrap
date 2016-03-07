/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50624
Source Host           : localhost:3306
Source Database       : androidlxc

Target Server Type    : MYSQL
Target Server Version : 50624
File Encoding         : 65001

Date: 2016-03-07 21:18:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ip
-- ----------------------------
DROP TABLE IF EXISTS `ip`;
CREATE TABLE `ip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) DEFAULT NULL,
  `inuse` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ip
-- ----------------------------
INSERT INTO `ip` VALUES ('1', '10.0.3.2', '1');
INSERT INTO `ip` VALUES ('2', '10.0.3.3', '0');
INSERT INTO `ip` VALUES ('3', '10.0.3.4', '0');
INSERT INTO `ip` VALUES ('4', '10.0.3.5', '0');
INSERT INTO `ip` VALUES ('5', '10.0.3.6', '0');
INSERT INTO `ip` VALUES ('6', '10.0.3.7', '0');
INSERT INTO `ip` VALUES ('7', '10.0.3.8', '0');
INSERT INTO `ip` VALUES ('8', '10.0.3.9', '0');
INSERT INTO `ip` VALUES ('9', '10.0.3.10', '0');
INSERT INTO `ip` VALUES ('10', '10.0.3.11', '0');
INSERT INTO `ip` VALUES ('11', '10.0.3.12', '0');
INSERT INTO `ip` VALUES ('12', '10.0.3.13', '0');
INSERT INTO `ip` VALUES ('13', '10.0.3.14', '0');
INSERT INTO `ip` VALUES ('14', '10.0.3.15', '0');
INSERT INTO `ip` VALUES ('15', '10.0.3.16', '0');
INSERT INTO `ip` VALUES ('16', '10.0.3.17', '0');
INSERT INTO `ip` VALUES ('17', '10.0.3.18', '0');
INSERT INTO `ip` VALUES ('18', '10.0.3.19', '0');
INSERT INTO `ip` VALUES ('19', '10.0.3.20', '0');
INSERT INTO `ip` VALUES ('20', '10.0.3.21', '0');
INSERT INTO `ip` VALUES ('21', '10.0.3.22', '0');
INSERT INTO `ip` VALUES ('22', '10.0.3.23', '0');
INSERT INTO `ip` VALUES ('23', '10.0.3.24', '0');
INSERT INTO `ip` VALUES ('24', '10.0.3.25', '0');
INSERT INTO `ip` VALUES ('25', '10.0.3.26', '0');
INSERT INTO `ip` VALUES ('26', '10.0.3.27', '0');
INSERT INTO `ip` VALUES ('27', '10.0.3.28', '0');
INSERT INTO `ip` VALUES ('28', '10.0.3.29', '0');
INSERT INTO `ip` VALUES ('29', '10.0.3.30', '0');
INSERT INTO `ip` VALUES ('30', '10.0.3.31', '0');
INSERT INTO `ip` VALUES ('31', '10.0.3.32', '0');
INSERT INTO `ip` VALUES ('32', '10.0.3.33', '0');
INSERT INTO `ip` VALUES ('33', '10.0.3.34', '0');
INSERT INTO `ip` VALUES ('34', '10.0.3.35', '0');
INSERT INTO `ip` VALUES ('35', '10.0.3.36', '0');
INSERT INTO `ip` VALUES ('36', '10.0.3.37', '0');
INSERT INTO `ip` VALUES ('37', '10.0.3.38', '0');
INSERT INTO `ip` VALUES ('38', '10.0.3.39', '0');
INSERT INTO `ip` VALUES ('39', '10.0.3.40', '0');
INSERT INTO `ip` VALUES ('40', '10.0.3.41', '0');
INSERT INTO `ip` VALUES ('41', '10.0.3.42', '0');
INSERT INTO `ip` VALUES ('42', '10.0.3.43', '0');
INSERT INTO `ip` VALUES ('43', '10.0.3.44', '0');
INSERT INTO `ip` VALUES ('44', '10.0.3.45', '0');
INSERT INTO `ip` VALUES ('45', '10.0.3.46', '0');
INSERT INTO `ip` VALUES ('46', '10.0.3.47', '0');
INSERT INTO `ip` VALUES ('47', '10.0.3.48', '0');
INSERT INTO `ip` VALUES ('48', '10.0.3.49', '0');
INSERT INTO `ip` VALUES ('49', '10.0.3.50', '0');
INSERT INTO `ip` VALUES ('50', '10.0.3.51', '0');
INSERT INTO `ip` VALUES ('51', '10.0.3.52', '0');
INSERT INTO `ip` VALUES ('52', '10.0.3.53', '0');
INSERT INTO `ip` VALUES ('53', '10.0.3.54', '0');
INSERT INTO `ip` VALUES ('54', '10.0.3.55', '0');
INSERT INTO `ip` VALUES ('55', '10.0.3.56', '0');
INSERT INTO `ip` VALUES ('56', '10.0.3.57', '0');
INSERT INTO `ip` VALUES ('57', '10.0.3.58', '0');
INSERT INTO `ip` VALUES ('58', '10.0.3.59', '0');
INSERT INTO `ip` VALUES ('59', '10.0.3.60', '0');
INSERT INTO `ip` VALUES ('60', '10.0.3.61', '0');
INSERT INTO `ip` VALUES ('61', '10.0.3.62', '0');
INSERT INTO `ip` VALUES ('62', '10.0.3.63', '0');
INSERT INTO `ip` VALUES ('63', '10.0.3.64', '0');
INSERT INTO `ip` VALUES ('64', '10.0.3.65', '0');
INSERT INTO `ip` VALUES ('65', '10.0.3.66', '0');
INSERT INTO `ip` VALUES ('66', '10.0.3.67', '0');
INSERT INTO `ip` VALUES ('67', '10.0.3.68', '0');
INSERT INTO `ip` VALUES ('68', '10.0.3.69', '0');
INSERT INTO `ip` VALUES ('69', '10.0.3.70', '0');
INSERT INTO `ip` VALUES ('70', '10.0.3.71', '0');
INSERT INTO `ip` VALUES ('71', '10.0.3.72', '0');
INSERT INTO `ip` VALUES ('72', '10.0.3.73', '0');
INSERT INTO `ip` VALUES ('73', '10.0.3.74', '0');
INSERT INTO `ip` VALUES ('74', '10.0.3.75', '0');
INSERT INTO `ip` VALUES ('75', '10.0.3.76', '0');
INSERT INTO `ip` VALUES ('76', '10.0.3.77', '0');
INSERT INTO `ip` VALUES ('77', '10.0.3.78', '0');
INSERT INTO `ip` VALUES ('78', '10.0.3.79', '0');
INSERT INTO `ip` VALUES ('79', '10.0.3.80', '0');
INSERT INTO `ip` VALUES ('80', '10.0.3.81', '0');
INSERT INTO `ip` VALUES ('81', '10.0.3.82', '0');
INSERT INTO `ip` VALUES ('82', '10.0.3.83', '0');
INSERT INTO `ip` VALUES ('83', '10.0.3.84', '0');
INSERT INTO `ip` VALUES ('84', '10.0.3.85', '0');
INSERT INTO `ip` VALUES ('85', '10.0.3.86', '0');
INSERT INTO `ip` VALUES ('86', '10.0.3.87', '0');
INSERT INTO `ip` VALUES ('87', '10.0.3.88', '0');
INSERT INTO `ip` VALUES ('88', '10.0.3.89', '0');
INSERT INTO `ip` VALUES ('89', '10.0.3.90', '0');
INSERT INTO `ip` VALUES ('90', '10.0.3.91', '0');
INSERT INTO `ip` VALUES ('91', '10.0.3.92', '0');
INSERT INTO `ip` VALUES ('92', '10.0.3.93', '0');
INSERT INTO `ip` VALUES ('93', '10.0.3.94', '0');
INSERT INTO `ip` VALUES ('94', '10.0.3.95', '0');
INSERT INTO `ip` VALUES ('95', '10.0.3.96', '0');
INSERT INTO `ip` VALUES ('96', '10.0.3.97', '0');
INSERT INTO `ip` VALUES ('97', '10.0.3.98', '0');
INSERT INTO `ip` VALUES ('98', '10.0.3.99', '0');
INSERT INTO `ip` VALUES ('99', '10.0.3.100', '0');
INSERT INTO `ip` VALUES ('100', '10.0.3.101', '0');
INSERT INTO `ip` VALUES ('101', '10.0.3.102', '0');
INSERT INTO `ip` VALUES ('102', '10.0.3.103', '0');
INSERT INTO `ip` VALUES ('103', '10.0.3.104', '0');
INSERT INTO `ip` VALUES ('104', '10.0.3.105', '0');
INSERT INTO `ip` VALUES ('105', '10.0.3.106', '0');
INSERT INTO `ip` VALUES ('106', '10.0.3.107', '0');
INSERT INTO `ip` VALUES ('107', '10.0.3.108', '0');
INSERT INTO `ip` VALUES ('108', '10.0.3.109', '0');
INSERT INTO `ip` VALUES ('109', '10.0.3.110', '0');
INSERT INTO `ip` VALUES ('110', '10.0.3.111', '0');
INSERT INTO `ip` VALUES ('111', '10.0.3.112', '0');
INSERT INTO `ip` VALUES ('112', '10.0.3.113', '0');
INSERT INTO `ip` VALUES ('113', '10.0.3.114', '0');
INSERT INTO `ip` VALUES ('114', '10.0.3.115', '0');
INSERT INTO `ip` VALUES ('115', '10.0.3.116', '0');
INSERT INTO `ip` VALUES ('116', '10.0.3.117', '0');
INSERT INTO `ip` VALUES ('117', '10.0.3.118', '0');
INSERT INTO `ip` VALUES ('118', '10.0.3.119', '0');
INSERT INTO `ip` VALUES ('119', '10.0.3.120', '0');
INSERT INTO `ip` VALUES ('120', '10.0.3.121', '0');
INSERT INTO `ip` VALUES ('121', '10.0.3.122', '0');
INSERT INTO `ip` VALUES ('122', '10.0.3.123', '0');
INSERT INTO `ip` VALUES ('123', '10.0.3.124', '0');
INSERT INTO `ip` VALUES ('124', '10.0.3.125', '0');
INSERT INTO `ip` VALUES ('125', '10.0.3.126', '0');
INSERT INTO `ip` VALUES ('126', '10.0.3.127', '0');
INSERT INTO `ip` VALUES ('127', '10.0.3.128', '0');
INSERT INTO `ip` VALUES ('128', '10.0.3.129', '0');
INSERT INTO `ip` VALUES ('129', '10.0.3.130', '0');
INSERT INTO `ip` VALUES ('130', '10.0.3.131', '0');
INSERT INTO `ip` VALUES ('131', '10.0.3.132', '0');
INSERT INTO `ip` VALUES ('132', '10.0.3.133', '0');
INSERT INTO `ip` VALUES ('133', '10.0.3.134', '0');
INSERT INTO `ip` VALUES ('134', '10.0.3.135', '0');
INSERT INTO `ip` VALUES ('135', '10.0.3.136', '0');
INSERT INTO `ip` VALUES ('136', '10.0.3.137', '0');
INSERT INTO `ip` VALUES ('137', '10.0.3.138', '0');
INSERT INTO `ip` VALUES ('138', '10.0.3.139', '0');
INSERT INTO `ip` VALUES ('139', '10.0.3.140', '0');
INSERT INTO `ip` VALUES ('140', '10.0.3.141', '0');
INSERT INTO `ip` VALUES ('141', '10.0.3.142', '0');
INSERT INTO `ip` VALUES ('142', '10.0.3.143', '0');
INSERT INTO `ip` VALUES ('143', '10.0.3.144', '0');
INSERT INTO `ip` VALUES ('144', '10.0.3.145', '0');
INSERT INTO `ip` VALUES ('145', '10.0.3.146', '0');
INSERT INTO `ip` VALUES ('146', '10.0.3.147', '0');
INSERT INTO `ip` VALUES ('147', '10.0.3.148', '0');
INSERT INTO `ip` VALUES ('148', '10.0.3.149', '0');
INSERT INTO `ip` VALUES ('149', '10.0.3.150', '0');
INSERT INTO `ip` VALUES ('150', '10.0.3.151', '0');
INSERT INTO `ip` VALUES ('151', '10.0.3.152', '0');
INSERT INTO `ip` VALUES ('152', '10.0.3.153', '0');
INSERT INTO `ip` VALUES ('153', '10.0.3.154', '0');
INSERT INTO `ip` VALUES ('154', '10.0.3.155', '0');
INSERT INTO `ip` VALUES ('155', '10.0.3.156', '0');
INSERT INTO `ip` VALUES ('156', '10.0.3.157', '0');
INSERT INTO `ip` VALUES ('157', '10.0.3.158', '0');
INSERT INTO `ip` VALUES ('158', '10.0.3.159', '0');
INSERT INTO `ip` VALUES ('159', '10.0.3.160', '0');
INSERT INTO `ip` VALUES ('160', '10.0.3.161', '0');
INSERT INTO `ip` VALUES ('161', '10.0.3.162', '0');
INSERT INTO `ip` VALUES ('162', '10.0.3.163', '0');
INSERT INTO `ip` VALUES ('163', '10.0.3.164', '0');
INSERT INTO `ip` VALUES ('164', '10.0.3.165', '0');
INSERT INTO `ip` VALUES ('165', '10.0.3.166', '0');
INSERT INTO `ip` VALUES ('166', '10.0.3.167', '0');
INSERT INTO `ip` VALUES ('167', '10.0.3.168', '0');
INSERT INTO `ip` VALUES ('168', '10.0.3.169', '0');
INSERT INTO `ip` VALUES ('169', '10.0.3.170', '0');
INSERT INTO `ip` VALUES ('170', '10.0.3.171', '0');
INSERT INTO `ip` VALUES ('171', '10.0.3.172', '0');
INSERT INTO `ip` VALUES ('172', '10.0.3.173', '0');
INSERT INTO `ip` VALUES ('173', '10.0.3.174', '0');
INSERT INTO `ip` VALUES ('174', '10.0.3.175', '0');
INSERT INTO `ip` VALUES ('175', '10.0.3.176', '0');
INSERT INTO `ip` VALUES ('176', '10.0.3.177', '0');
INSERT INTO `ip` VALUES ('177', '10.0.3.178', '0');
INSERT INTO `ip` VALUES ('178', '10.0.3.179', '0');
INSERT INTO `ip` VALUES ('179', '10.0.3.180', '0');
INSERT INTO `ip` VALUES ('180', '10.0.3.181', '0');
INSERT INTO `ip` VALUES ('181', '10.0.3.182', '0');
INSERT INTO `ip` VALUES ('182', '10.0.3.183', '0');
INSERT INTO `ip` VALUES ('183', '10.0.3.184', '0');
INSERT INTO `ip` VALUES ('184', '10.0.3.185', '0');
INSERT INTO `ip` VALUES ('185', '10.0.3.186', '0');
INSERT INTO `ip` VALUES ('186', '10.0.3.187', '0');
INSERT INTO `ip` VALUES ('187', '10.0.3.188', '0');
INSERT INTO `ip` VALUES ('188', '10.0.3.189', '0');
INSERT INTO `ip` VALUES ('189', '10.0.3.190', '0');
INSERT INTO `ip` VALUES ('190', '10.0.3.191', '0');
INSERT INTO `ip` VALUES ('191', '10.0.3.192', '0');
INSERT INTO `ip` VALUES ('192', '10.0.3.193', '0');
INSERT INTO `ip` VALUES ('193', '10.0.3.194', '0');
INSERT INTO `ip` VALUES ('194', '10.0.3.195', '0');
INSERT INTO `ip` VALUES ('195', '10.0.3.196', '0');
INSERT INTO `ip` VALUES ('196', '10.0.3.197', '0');
INSERT INTO `ip` VALUES ('197', '10.0.3.198', '0');
INSERT INTO `ip` VALUES ('198', '10.0.3.199', '0');
INSERT INTO `ip` VALUES ('199', '10.0.3.200', '0');
INSERT INTO `ip` VALUES ('200', '10.0.3.201', '0');
INSERT INTO `ip` VALUES ('201', '10.0.3.202', '0');
INSERT INTO `ip` VALUES ('202', '10.0.3.203', '0');
INSERT INTO `ip` VALUES ('203', '10.0.3.204', '0');
INSERT INTO `ip` VALUES ('204', '10.0.3.205', '0');
INSERT INTO `ip` VALUES ('205', '10.0.3.206', '0');
INSERT INTO `ip` VALUES ('206', '10.0.3.207', '0');
INSERT INTO `ip` VALUES ('207', '10.0.3.208', '0');
INSERT INTO `ip` VALUES ('208', '10.0.3.209', '0');
INSERT INTO `ip` VALUES ('209', '10.0.3.210', '0');
INSERT INTO `ip` VALUES ('210', '10.0.3.211', '0');
INSERT INTO `ip` VALUES ('211', '10.0.3.212', '0');
INSERT INTO `ip` VALUES ('212', '10.0.3.213', '0');
INSERT INTO `ip` VALUES ('213', '10.0.3.214', '0');
INSERT INTO `ip` VALUES ('214', '10.0.3.215', '0');
INSERT INTO `ip` VALUES ('215', '10.0.3.216', '0');
INSERT INTO `ip` VALUES ('216', '10.0.3.217', '0');
INSERT INTO `ip` VALUES ('217', '10.0.3.218', '0');
INSERT INTO `ip` VALUES ('218', '10.0.3.219', '0');
INSERT INTO `ip` VALUES ('219', '10.0.3.220', '0');
INSERT INTO `ip` VALUES ('220', '10.0.3.221', '0');
INSERT INTO `ip` VALUES ('221', '10.0.3.222', '0');
INSERT INTO `ip` VALUES ('222', '10.0.3.223', '0');
INSERT INTO `ip` VALUES ('223', '10.0.3.224', '0');
INSERT INTO `ip` VALUES ('224', '10.0.3.225', '0');
INSERT INTO `ip` VALUES ('225', '10.0.3.226', '0');
INSERT INTO `ip` VALUES ('226', '10.0.3.227', '0');
INSERT INTO `ip` VALUES ('227', '10.0.3.228', '0');
INSERT INTO `ip` VALUES ('228', '10.0.3.229', '0');
INSERT INTO `ip` VALUES ('229', '10.0.3.230', '0');
INSERT INTO `ip` VALUES ('230', '10.0.3.231', '0');
INSERT INTO `ip` VALUES ('231', '10.0.3.232', '0');
INSERT INTO `ip` VALUES ('232', '10.0.3.233', '0');
INSERT INTO `ip` VALUES ('233', '10.0.3.234', '0');
INSERT INTO `ip` VALUES ('234', '10.0.3.235', '0');
INSERT INTO `ip` VALUES ('235', '10.0.3.236', '0');
INSERT INTO `ip` VALUES ('236', '10.0.3.237', '0');
INSERT INTO `ip` VALUES ('237', '10.0.3.238', '0');
INSERT INTO `ip` VALUES ('238', '10.0.3.239', '0');
INSERT INTO `ip` VALUES ('239', '10.0.3.240', '0');
INSERT INTO `ip` VALUES ('240', '10.0.3.241', '0');
INSERT INTO `ip` VALUES ('241', '10.0.3.242', '0');
INSERT INTO `ip` VALUES ('242', '10.0.3.243', '0');
INSERT INTO `ip` VALUES ('243', '10.0.3.244', '0');
INSERT INTO `ip` VALUES ('244', '10.0.3.245', '0');
INSERT INTO `ip` VALUES ('245', '10.0.3.246', '0');
INSERT INTO `ip` VALUES ('246', '10.0.3.247', '0');
INSERT INTO `ip` VALUES ('247', '10.0.3.248', '0');
INSERT INTO `ip` VALUES ('248', '10.0.3.249', '0');
INSERT INTO `ip` VALUES ('249', '10.0.3.250', '0');
INSERT INTO `ip` VALUES ('250', '10.0.3.251', '0');
INSERT INTO `ip` VALUES ('251', '10.0.3.252', '0');
INSERT INTO `ip` VALUES ('252', '10.0.3.253', '0');
INSERT INTO `ip` VALUES ('253', '10.0.3.254', '0');

-- ----------------------------
-- Table structure for lxc
-- ----------------------------
DROP TABLE IF EXISTS `lxc`;
CREATE TABLE `lxc` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `status` smallint(11) DEFAULT '-1',
  `ip` varchar(255) DEFAULT NULL,
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
  `ctime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `mem` int(11) DEFAULT '100',
  `cpuset` varchar(255) DEFAULT NULL,
  `cpushare` int(11) DEFAULT '1024',
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of lxc
-- ----------------------------
INSERT INTO `lxc` VALUES ('1', 'be14b3c3-ebe8-422b-b720-33add721e677', '2', '10.0.3.2', '2015-12-21 16:25:41', '0000-00-00 00:00:00', '100', null, '1024', null);

-- ----------------------------
-- Procedure structure for insertip
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertip`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `insertip`()
begin
 
declare num int;
 
set num=4;
 
while num < 255 do
 
insert into ip(ip) values(concat("10.0.3.",num)); set num=num+1;
 
end while;
 
end
;;
DELIMITER ;
