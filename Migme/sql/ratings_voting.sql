CREATE TABLE `avatarrating` (
  `AvatarUserID` int(10) unsigned NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Rating` tinyint(2) NOT NULL,
  PRIMARY KEY  (`AvatarUserID`,`UserID`),
  KEY `FK_avatarrating_userid` (`UserID`),
  CONSTRAINT `FK_avatarrating_avataruserid` FOREIGN KEY (`AvatarUserID`) REFERENCES `userid` (`id`),
  CONSTRAINT `FK_avatarrating_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `avatarratingsummary` (
  `AvatarUserID` int(10) unsigned NOT NULL,
  `Average` double(7,5) unsigned NOT NULL default '0.00000',
  `Total` int(11) NOT NULL default '0',
  `NumRatings` int(11) NOT NULL default '0',
  PRIMARY KEY  (`AvatarUserID`),
  CONSTRAINT `FK_avatarratingsummary_avataruserid` FOREIGN KEY (`AvatarUserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouplike` (
  `GroupID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`GroupID`,`UserID`),
  KEY `FK_grouplike_userid` (`UserID`),
  CONSTRAINT `FK_grouplike_groupid` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `FK_grouplike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouplikesummary` (
  `GroupID` int(11) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  PRIMARY KEY  (`GroupID`),
  CONSTRAINT `FK_grouplikesummary_groupid` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `scrapbooklike` (
  `ScrapbookID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`ScrapbookID`,`UserID`),
  KEY `FK_scrapbooklike_userid` (`UserID`),
  CONSTRAINT `FK_scrapbooklike_scrapbookid` FOREIGN KEY (`ScrapbookID`) REFERENCES `scrapbook` (`ID`),
  CONSTRAINT `FK_scrapbooklike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `scrapbooklikesummary` (
  `ScrapbookID` int(11) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ScrapbookID`),
  CONSTRAINT `FK_scrapbooklikesummary_scrapbookid` FOREIGN KEY (`ScrapbookID`) REFERENCES `scrapbook` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `storeitemrating` (
  `StoreItemID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Rating` tinyint(2) NOT NULL,
  PRIMARY KEY  (`StoreItemID`,`UserID`),
  KEY `FK_storeitemrating_userid` (`UserID`),
  CONSTRAINT `FK_storeitemrating_storeitemid` FOREIGN KEY (`StoreItemID`) REFERENCES `storeitem` (`ID`),
  CONSTRAINT `FK_storeitemrating_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `storeitemratingsummary` (
  `StoreItemID` int(11) NOT NULL,
  `Average` double(7,5) unsigned NOT NULL default '0.00000',
  `Total` int(11) NOT NULL default '0',
  `NumRatings` int(11) NOT NULL default '0',
  PRIMARY KEY  (`StoreItemID`),
  CONSTRAINT `FK_storeitemratingsummary_storeitemid` FOREIGN KEY (`StoreItemID`) REFERENCES `storeitem` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `virtualgiftreceivedlike` (
  `VirtualGiftReceivedID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`VirtualGiftReceivedID`,`UserID`),
  KEY `FK_virtualgiftreceivedlike_userid` (`UserID`),
  CONSTRAINT `FK_virtualgiftreceivedlike_virtualgiftreceivedid` FOREIGN KEY (`VirtualGiftReceivedID`) REFERENCES `virtualgiftreceived` (`ID`),
  CONSTRAINT `FK_virtualgiftreceivedlike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `virtualgiftreceivedlikesummary` (
  `VirtualGiftReceivedID` int(11) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  PRIMARY KEY  (`VirtualGiftReceivedID`),
  CONSTRAINT `FK_virtualgiftreceivedlikesummary_virtualgiftreceivedid` FOREIGN KEY (`VirtualGiftReceivedID`) REFERENCES `virtualgiftreceived` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;