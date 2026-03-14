CREATE TABLE `avatarcomment` (
  `ID` int(10) unsigned NOT NULL auto_increment,
  `AvatarUserID` int(10) unsigned NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Comment` varchar(4000) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_avatarcomment_avataruserid` (`AvatarUserID`),
  KEY `FK_avatarcomment_userid` (`UserID`),
  CONSTRAINT `FK_avatarcomment_avataruserid` FOREIGN KEY (`AvatarUserID`) REFERENCES `userid` (`id`),
  CONSTRAINT `FK_avatarcomment_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `avatarcommentlike` (
  `AvatarCommentID` int(10) unsigned NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`AvatarCommentID`,`UserID`),
  KEY `FK_avatarcommentlike_userid` (`UserID`),
  CONSTRAINT `FK_avatarcommentlike_avatarcommentid` FOREIGN KEY (`AvatarCommentID`) REFERENCES `avatarcomment` (`id`),
  CONSTRAINT `FK_avatarcommentlike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `virtualgiftreceivedcomment` (
  `ID` int(10) unsigned NOT NULL auto_increment,
  `VirtualGiftReceivedID` int(11) NOT NULL,
  `VirtualGiftReceivedUserID` int(10) unsigned NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Comment` varchar(4000) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_virtualgiftreceivedcomment_virtualgiftreceivedid` (`VirtualGiftReceivedID`),
  KEY `FK_virtualgiftreceivedcomment_virtualgiftreceiveduserid` (`VirtualGiftReceivedUserID`),
  KEY `FK_virtualgiftreceivedcomment_userid` (`UserID`),
  CONSTRAINT `FK_virtualgiftreceivedcomment_virtualgiftreceivedid` FOREIGN KEY (`VirtualGiftReceivedID`) REFERENCES `virtualgiftreceived` (`ID`),
  CONSTRAINT `FK_virtualgiftreceivedcomment_virtualgiftreceiveduserid` FOREIGN KEY (`VirtualGiftReceivedUserID`) REFERENCES `userid` (`ID`),
  CONSTRAINT `FK_virtualgiftreceivedcomment_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `virtualgiftreceivedcommentlike` (
  `VirtualGiftReceivedCommentID` int(10) unsigned NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`VirtualGiftReceivedCommentID`,`UserID`),
  KEY `FK_virtualgiftreceivedcommentlike_userid` (`UserID`),
  CONSTRAINT `FK_virtualgiftreceivedcommentlike_virtualgiftreceivedcommentid` FOREIGN KEY (`VirtualGiftReceivedCommentID`) REFERENCES `virtualgiftreceivedcomment` (`id`),
  CONSTRAINT `FK_virtualgiftreceivedcommentlike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;