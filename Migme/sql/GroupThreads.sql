CREATE TABLE `userpost` (
  `ID` int(11) NOT NULL auto_increment,
  `Username` varchar(128) NOT NULL,
  `Body` text NULL,
  `DateCreated` datetime NOT NULL,
  `ParentUserPostID` int(11) NULL,
  `NumReplies` int(11) NOT NULL DEFAULT 0,
  `LastReplyDate` datetime NOT NULL,
  `Status` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`Username`) REFERENCES `user` (`Username`),
  FOREIGN KEY (`ParentUserPostID`) REFERENCES `userpost` (`ID`),
  INDEX (`ID`, `ParentUserPostID`, `LastReplyDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupuserpost` (
  `GroupID` int(11) NOT NULL,
  `UserPostID` int(11) NOT NULL,
  PRIMARY KEY  (`GroupID`, `UserPostID`),
  FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  FOREIGN KEY (`UserPostID`) REFERENCES `userpost` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE groupmember ADD COLUMN `EmailThreadUpdateNotification` int(11) NOT NULL DEFAULT 0 AFTER `SMSGroupEventNotification`;
ALTER TABLE groupmember ADD COLUMN `EventThreadUpdateNotification` int(11) NOT NULL DEFAULT 0 AFTER `EmailThreadUpdateNotification`;

insert into system values
('GroupThreadUpdatedEmailSubject','New comment on "<topic_trunc>"'),
('GroupThreadUpdatedEmailBody','<poster_username> has commented on the topic "<topic>" in the <group_name> group.\n\n<poster_username> said...\n"<comment>"\n\nDon''t want to receive these emails? You can disable notifications in the <group_name> group by selecting the "Options" link.');
