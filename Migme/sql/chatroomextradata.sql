CREATE TABLE `chatroomextradata` (
  `chatroomid` int(11) unsigned NOT NULL,
  `type` int NOT NULL,
  `value` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`chatroomid`, `type`),
  CONSTRAINT `FK_chatroomextradata_chatroomid` FOREIGN KEY (`chatroomid`) REFERENCES `chatroom` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
