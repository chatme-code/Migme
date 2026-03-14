
insert into groups (id, countryid, name, description, about, datecreated, createdby, picture, emailaddress, status)
values (7, NULL, 'Cricket Fans', 'A group for cricket fans around the world to watch matches together.',
'Connect with other cricket fans around the world about the latest events in cricket. View live match ball-by-ball commentary in chat rooms.', now(), 'cricketfans',
'a42d25a1088844b6985a32679713b831', 'cricketfans@mig33.com', 1);

insert into groupmember (username, groupid, datecreated, type, smsnotification, emailnotification, eventnotification, status) values ('cricketfans', 7, now(), 2, 0, 1, 1, 1);

update menu set countryid=NULL where title='My Groups';

update groups set countryid=NULL where id<7;

insert into emote (command, action, actionwithtarget) values
('bowled','','**%s bowled %t**'),
('stumped','','**%s caught %t sleeping and stumped him**'),
('hitforsix','**%s smashes it out of the ground for a six!**',''),
('whatacatch','**%s dives for an amazing catch**',''),
('spin','','**%s spins bowls around %t**');

insert into theme (Name, Description, Location, Status) values ('Cricket', 'Cricket', '/usr/fusion/themes/cricket', 1);

insert into groupannouncement(groupid, datecreated, createdby, title, text, lastmodifieddate, lastmodifiedby, status) values
(7, now(), 'cricketfans', 'Welcome', 'Welcome to Cricket Fans, a group moderated by mig33 for cricket fans around the world. Chat with other fans in any of the chat rooms. Special chat rooms will be available during matches with live ball-by-ball commentary.', now(), 'cricketfans', 1),
(7, now(), 'cricketfans', 'Schedule', 'Upcoming matches:<br>April 9 12:30 GMT, SA v AUS, 3rd ODI<br>April 17 12:30 GMT, SA v AUS, 4th ODI<br>April 18 10:30 GMT, Chennai v Mumbai, IPL 1st match', now(), 'cricketfans', 1);

insert into chatroom (name, type, creator, groupid, adultonly, maximumsize, datecreated, status)
values ('Cricket Fans 1', 1, 'cricketfans', 7, 0, 25, now(), 1),
('Cricket Fans 2', 1, 'cricketfans', 7, 0, 25, now(), 1),
('Cricket Fans 3', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 1', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 2', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 3', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 4', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 5', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 6', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 7', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 8', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 9', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 10', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 11', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 12', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 13', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 14', 1, 'cricketfans', 7, 0, 25, now(), 1),
('3rd ODI SA v Aus 15', 1, 'cricketfans', 7, 0, 25, now(), 1);

CREATE TABLE `chatroomcategory` (
  `ID` int(11) NOT NULL auto_increment,
  `Name` varchar(128) NOT NULL,
  `ParentChatRoomCategoryID` int(11) default NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_ChatRoomCategory` (`ParentChatRoomCategoryID`),
  CONSTRAINT `FK_ChatRoomCategory` FOREIGN KEY (`ParentChatRoomCategoryID`) REFERENCES `chatroomcategory` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE chatroom ADD COLUMN ChatRoomCategoryID INT(11) DEFAULT NULL AFTER `Creator`;
ALTER TABLE chatroom ADD CONSTRAINT `FK_ChatRoomCategory_2` FOREIGN KEY (`ChatRoomCategoryID`) REFERENCES `chatroomcategory` (`ID`);

ALTER TABLE groups ADD COLUMN Premium INT(11) DEFAULT 0 NOT NULL AFTER `EmailAddress`;

CREATE TABLE `groupmembershipcost` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `Description` varchar(128) NOT NULL,
  `Cost` double NULL,
  `Currency` varchar(6) NULL,
  `DurationDays` int(11) NOT NULL,
  `Status` int(11) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_GroupID` (`GroupID`),
  KEY `FK_Currency` (`Currency`),
  CONSTRAINT `GroupID` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  CONSTRAINT `Currency` FOREIGN KEY (`Currency`) REFERENCES `currency` (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupmembershipcoupon` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `CouponCode` varchar(128) NOT NULL,
  `DurationDays` int(11) NOT NULL,
  `RedeemedDate` datetime default NULL,
  `RedeemedBy` varchar(128) default NULL,
  `Status` int(11) NOT NULL,
  PRIMARY KEY  (`ID`),
  INDEX `IDX_GroupID_CouponCode` (`GroupID`, `CouponCode`),
  CONSTRAINT `FK_GroupID` FOREIGN KEY `FK_GroupID` (`GroupID`) REFERENCES `groups` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE groupmember ADD COLUMN ExpirationDate datetime DEFAULT NULL AFTER `DateLeft`;

