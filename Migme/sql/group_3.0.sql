CREATE TABLE `grouprss` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `Name` varchar(64) NOT NULL default '',
  `Url` varchar(128) NOT NULL,
  `LastUpdated` datetime default NULL,
  PRIMARY KEY  (`ID`),
  KEY `GroupID` (`GroupID`),
  CONSTRAINT `grouprss_ibfk_1` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE groups ADD COLUMN `Featured` tinyint(4) NOT NULL default '0',
	ADD COLUMN `Official` tinyint(4) NOT NULL default '0',
    ADD COLUMN `NumPhotos` int(11) NOT NULL default '0',
	ADD COLUMN `NumForumPosts` int(11) NOT NULL default '0';


ALTER TABLE reputationscoretolevel ADD COLUMN `GroupStorageSize` int(11) NOT NULL default '0' AFTER `GroupSize`;


UPDATE reputationscoretolevel SET groupsize=100 WHERE level>=20 AND level<=49;
UPDATE reputationscoretolevel SET groupsize=200 WHERE level>=50 AND level<=79;
UPDATE reputationscoretolevel SET groupsize=500 WHERE level=80;


CREATE TABLE `groupphotoalbum` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `Name` varchar(255) NOT NULL,
  `Description` varchar(1024) default NULL,
  `DateCreated` datetime NOT NULL default '0000-00-00 00:00:00',
  `CoverPhotoID` int(11) default NULL,
  `NumPhotos` int(7) NOT NULL default '0',
  `Size` int(11) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_groupphotoalbum_1` (`GroupID`),
  KEY `FK_groupphotoalbum_2` (`UserID`),
  KEY `FK_groupphotoalbum_3` (`CoverPhotoID`),
  CONSTRAINT `FK_groupphotoalbum_1` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `FK_groupphotoalbum_2` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupphoto` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupPhotoAlbumID` int(11) NOT NULL,
  `GroupID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `FileID` varchar(128) NOT NULL,
  `DateCreated` datetime NOT NULL default '0000-00-00 00:00:00',
  `Description` varchar(1024) default NULL,
  `NumComments` int(7) NOT NULL default '0',
  `NumLikes` int(7) NOT NULL default '0',
  `NumDislikes` int(7) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_groupphoto_1` (`GroupPhotoAlbumID`),
  KEY `FK_groupphoto_2` (`GroupID`),
  KEY `FK_groupphoto_3` (`UserID`),
  KEY `FK_groupphoto_4` (`FileID`),
  CONSTRAINT `FK_groupphoto_1` FOREIGN KEY (`GroupPhotoAlbumID`) REFERENCES `groupphotoalbum` (`ID`),
  CONSTRAINT `FK_groupphoto_2` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `FK_groupphoto_3` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`),
  CONSTRAINT `FK_groupphoto_4` FOREIGN KEY (`FileID`) REFERENCES `file` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `groupphotoalbum` ADD CONSTRAINT `FK_groupphotoalbum_3` FOREIGN KEY (`CoverPhotoID`) REFERENCES `groupphoto` (`ID`);

CREATE TABLE `groupphotolike` (
  `GroupPhotoID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`GroupPhotoID`,`UserID`),
  KEY `FK_groupphotolike_userid` (`UserID`),
  CONSTRAINT `FK_groupphotolike_groupphotoid` FOREIGN KEY (`GroupPhotoID`) REFERENCES `groupphoto` (`ID`),
  CONSTRAINT `FK_groupphotolike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupphotocomment` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupPhotoID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Comment` varchar(4000) NOT NULL,
  `NumLikes` int(11) NOT NULL default '0',
  `NumDislikes` int(11) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_groupphotocomment_groupphotoid` (`GroupPhotoID`),
  KEY `FK_groupphotocomment_userid` (`UserID`),
  CONSTRAINT `FK_groupphotocomment_groupphotoid` FOREIGN KEY (`GroupPhotoID`) REFERENCES `groupphoto` (`ID`),
  CONSTRAINT `FK_groupphotocomment_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupphotocommentlike` (
  `GroupPhotoCommentID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  `Type` tinyint(2) NOT NULL,
  PRIMARY KEY  (`GroupPhotoCommentID`,`UserID`),
  KEY `FK_groupphotocommentlike_userid` (`UserID`),
  CONSTRAINT `FK_groupphotocommentlike_groupphotocommentid` FOREIGN KEY (`GroupPhotoCommentID`) REFERENCES `groupphotocomment` (`ID`),
  CONSTRAINT `FK_groupphotocommentlike_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouppoll` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `Name` varchar(255) NOT NULL,
  `Description` varchar(1024) default NULL,
  `DateCreated` datetime NOT NULL default '0000-00-00 00:00:00',
  `DateExpiry` datetime NOT NULL default '0000-00-00 00:00:00',
  `TotalVotes` int(7) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_grouppoll_1` (`GroupID`),
  KEY `FK_grouppoll_2` (`UserID`),
  CONSTRAINT `FK_grouppoll_1` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `FK_grouppoll_2` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouppolloption` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupPollID` int(11) NOT NULL,
  `Name` varchar(255) NOT NULL,
  `NumVotes` int(7) NOT NULL default '0',
  `Status` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID`),
  KEY `FK_grouppolloption_grouppollid` (`GroupPollID`),
  CONSTRAINT `FK_grouppolloption_grouppollid` FOREIGN KEY (`GroupPollID`) REFERENCES `grouppoll` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouppollvote` (
  `GroupPollOptionID` int(11) NOT NULL,
  `GroupPollID` int(11) NOT NULL,
  `GroupID` int(11) NOT NULL,
  `UserID` int(10) unsigned NOT NULL,
  `DateCreated` datetime NOT NULL,
  PRIMARY KEY  (`GroupPollOptionID`,`UserID`),
  KEY `FK_grouppollvote_grouppollid` (`GroupPollID`),
  KEY `FK_grouppollvote_groupid` (`GroupID`),
  KEY `FK_grouppollvote_userid` (`UserID`),
  CONSTRAINT `FK_grouppollvote_grouppolloptionid` FOREIGN KEY (`GroupPollOptionID`) REFERENCES `grouppolloption` (`ID`),
  CONSTRAINT `FK_grouppollvote_grouppollid` FOREIGN KEY (`GroupPollID`) REFERENCES `grouppoll` (`ID`),
  CONSTRAINT `FK_grouppollvote_groupid` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `FK_grouppollvote_userid` FOREIGN KEY (`UserID`) REFERENCES `userid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `leaderboardgrouplikesalltime` (
  `Id` int(11) unsigned NOT NULL default '0',
  `Value` int(11) NOT NULL default '0',
  `Rank` int(11) NOT NULL auto_increment,
  PRIMARY KEY  (`Rank`),
  KEY `Value` (`Value`),
  KEY `Id` (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `leaderboardgroupphotosalltime` (
  `Id` int(11) unsigned NOT NULL default '0',
  `Value` int(11) NOT NULL default '0',
  `Rank` int(11) NOT NULL auto_increment,
  PRIMARY KEY  (`Rank`),
  KEY `Value` (`Value`),
  KEY `Id` (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `leaderboardgrouptopicsalltime` (
  `Id` int(11) unsigned NOT NULL default '0',
  `Value` int(11) NOT NULL default '0',
  `Rank` int(11) NOT NULL auto_increment,
  PRIMARY KEY  (`Rank`),
  KEY `Value` (`Value`),
  KEY `Id` (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupjoinrequest` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `DateCreated` datetime NOT NULL,
  `RequesterID` int(11) NOT NULL,
  `Status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`),
  KEY `FK_GroupInvitation_2` (`GroupID`),
  KEY `RequesterID` (`RequesterID`),
  CONSTRAINT `groupjoinrequest_ibfk_2` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into chatroom
(name, type, creator, groupid, adultonly, maximumsize, userowned, allowkicking, allowbots, language, datecreated, status)
select concat('Lobby ', g.id), 1, g.createdby, g.id, 0, 25, 1, 0, 1, 'ENG', now(), 1
from 
groups g

INSERT INTO groupforum (groupid, numposts, numcomments, numlikes, numdislikes, datecreated)
SELECT id, 0, 0, 0, 0, NOW()
FROM groups 
WHERE not exists(SELECT * FROM groupforum WHERE groupid=groups.id);

