/*
 Navicat Premium Data Transfer

 Source Server         : Trunk
 Source Server Type    : MySQL
 Source Server Version : 50045
 Source Host           : 192.168.1.131
 Source Database       : fusion

 Target Server Type    : MySQL
 Target Server Version : 50045
 File Encoding         : utf-8

 Date: 02/17/2010 17:08:44 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `avatarbody`
-- ----------------------------
DROP TABLE IF EXISTS `avatarbody`;
CREATE TABLE `avatarbody` (
  `id` int(11) NOT NULL auto_increment,
  `Name` varchar(128) NOT NULL default '',
  `Description` varchar(128) default NULL,
  `PreviewImage` varchar(128) NOT NULL,
  `Image` varchar(128) NOT NULL,
  `HeadX` int(11) NOT NULL default '0',
  `HeadY` int(11) NOT NULL default '0',
  `HeadWidth` int(11) NOT NULL default '0',
  `HeadHeight` int(11) NOT NULL default '0',
  `Gender` char(1) NOT NULL default 'M',
  `Status` int(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `avatarbody`
-- ----------------------------
INSERT INTO `avatarbody` VALUES ('1', '', null, 'AMbodydark-preview.png', 'AMbodydark.png', '25', '75', '100', '100', 'M', '1'), ('2', '', null, 'AMbodydarkest-preview.png', 'AMbodydarkest.png', '25', '75', '100', '100', 'M', '1'), ('3', '', null, 'AMbodyfair-preview.png', 'AMbodyfair.png', '25', '75', '100', '100', 'M', '1'), ('4', '', null, 'AMbodypale-preview.png', 'AMbodypale.png', '25', '75', '100', '100', 'M', '1'), ('5', '', null, 'AFbodydark-preview.png', 'AFbodydark.png', '25', '75', '100', '100', 'F', '1'), ('6', '', null, 'AFbodydarkest-preview.png', 'AFbodydarkest.png', '25', '75', '100', '100', 'F', '1'), ('7', '', null, 'AFbodyfair-preview.png', 'AFbodyfair.png', '25', '75', '100', '100', 'F', '1'), ('8', '', null, 'AFbodypale-preview.png', 'AFbodypale.png', '25', '75', '100', '100', 'F', '1');

