CREATE TABLE `registrationcontext` (
  `userid` int(10) unsigned NOT NULL,
  `type` int NOT NULL,
  `value` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`userid`, `type`),
  CONSTRAINT `FK_registrationcontext_userid` FOREIGN KEY (`userid`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
