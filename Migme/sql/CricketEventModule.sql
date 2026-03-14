CREATE TABLE `groupevent` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `Description` varchar(128) NOT NULL,
  `StartTime` datetime NOT NULL,
  `DurationMinutes` int(11) NULL,
  `ChatRoomName` varchar(128) NULL,
  `ChatRoomCategoryID` int(11) NULL,
  `DateCreated` datetime NOT NULL,
  `AlertSent` int(11) NOT NULL DEFAULT 0,
  `Status` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  FOREIGN KEY (`ChatRoomName`) REFERENCES `chatroom` (`Name`),
  FOREIGN KEY (`ChatRoomCategoryID`) REFERENCES `chatroomcategory` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `chatroomcategory` ADD COLUMN `GroupEventOnly` int(11) NOT NULL DEFAULT 0 AFTER `Name`;