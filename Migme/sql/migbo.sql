CREATE TABLE `useralias` (
  `username` varchar(128) NOT NULL,
  `alias` varchar(128) NOT NULL,
  `DateUpdated` datetime DEFAULT NULL,
  PRIMARY KEY (`username`),
  UNIQUE KEY `alias` (`alias`),
  CONSTRAINT `FK_useralias_username` FOREIGN KEY (`username`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
