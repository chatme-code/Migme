insert into `system` (`PropertyName`, `PropertyValue`) values('GroupInvitationAlert','%inviter% has invited you to join the %groupname% group. Please visit the My Groups section in your profile to respond.');
insert into `system` (`PropertyName`, `PropertyValue`) values('GroupJoinURL','http://www.mig33.com/sites/index.php?c=group&v=midlet&a=join&cid=');
insert into `system` (`PropertyName`, `PropertyValue`) values('GroupReferralSMS','Hi, %1 invites you into %4 group on mig33. Open http://m.mig33.com/join.php?m=%3&g=%5 on your phone or go 2 m.mig33.com');
insert into `system` (`PropertyName`, `PropertyValue`) values('GroupSMSNotificationCost','0.00');
insert into `system` (`PropertyName`, `PropertyValue`) values('AnonymousCalling','0');



CREATE TABLE `fusion`.`Groups` (
  `ID` INTEGER NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(128) NOT NULL,
  `Description` VARCHAR(128) NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  `CreatedBy` VARCHAR(128) NOT NULL,
  `Picture` VARCHAR(128),
  `EmailAddress` VARCHAR(128),
  `Status` INTEGER NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_Groups_1` FOREIGN KEY `FK_Groups_1` (`CreatedBy`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


alter table `fusion`.`groups` add column `CountryID` int(11) NULL after `ID`;
alter table `fusion`.`groups` add constraint `FK_Groups_2` FOREIGN KEY (`CountryID`) REFERENCES `country` (`ID`);
alter table `fusion`.`groups` add column `About` text  NOT NULL after `Description`;


CREATE TABLE `fusion`.`GroupAnnouncement` (
  `ID` INTEGER NOT NULL AUTO_INCREMENT,
  `GroupID` INTEGER NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  `CreatedBy` VARCHAR(128) NOT NULL,
  `Title` VARCHAR(128) NOT NULL,
  `Text` TEXT NOT NULL,
  `LastModifiedDate` DATETIME NOT NULL,
  `LastModifiedBy` VARCHAR(128) NOT NULL,
  `Status` INTEGER NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_GroupAnnouncement_1` FOREIGN KEY `FK_GroupAnnouncement_1` (`GroupID`)
    REFERENCES `groups` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_GroupAnnouncement_2` FOREIGN KEY `FK_GroupAnnouncement_2` (`CreatedBy`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_GroupAnnouncement_3` FOREIGN KEY `FK_GroupAnnouncement_3` (`LastModifiedBy`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


alter table `fusion`.`groupannouncement` 
	add column `SMSText` text  NULL after `Text`;


alter table `fusion`.`chatroom` 
  add column `Type` int(11) DEFAULT '1' NOT NULL after `Description`, 
  add column `GroupID` int(11) NULL after `SecondaryCountryID`,
  add constraint `FK_chatroom_4` FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT;

CREATE TABLE `fusion`.`GroupMember` (
  `ID` INTEGER NOT NULL AUTO_INCREMENT,
  `Username` VARCHAR(128) NOT NULL,
  `GroupID` INTEGER NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  `Type` INTEGER NOT NULL,
  `DateLeft` DATETIME,
  `SMSNotification` INTEGER NOT NULL,
  `Status` INTEGER NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `Index_GroupMember_1`(`Username`, `GroupID`),
  CONSTRAINT `FK_GroupMember_1` FOREIGN KEY `FK_GroupMember_1` (`Username`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_GroupMember_2` FOREIGN KEY `FK_GroupMember_2` (`GroupID`)
    REFERENCES `groups` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB CHARACTER SET utf8;

alter table `fusion`.`groupmember` add column `EmailNotification` int(11) NOT NULL after `SMSNotification`;
alter table `fusion`.`groupmember` add column `EventNotification` int(11) NOT NULL after `EmailNotification`;
alter table `fusion`.`groupmember` add column `LocationID` int(11) NULL after `GroupID`;
alter table `fusion`.`groupmember` add constraint `FK_GroupMember_3` FOREIGN KEY (`LocationID`) REFERENCES `location` (`ID`);


CREATE TABLE `fusion`.`GroupInvitation` (
  `ID` INTEGER NOT NULL AUTO_INCREMENT,
  `Username` VARCHAR(128) NOT NULL,
  `GroupID` INTEGER NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  `Inviter` VARCHAR(128)  NULL,
  `Status` INTEGER NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_GroupInvitation_1` FOREIGN KEY `FK_GroupInvitation_1` (`Username`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_GroupInvitation_2` FOREIGN KEY `FK_GroupInvitation_2` (`GroupID`)
    REFERENCES `groups` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_GroupInvitation_3` FOREIGN KEY `FK_GroupInvitation_3` (`Inviter`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB CHARACTER SET utf8;


alter table `fusion`.`chatroom` 
  add column `LocationID` int(11) NULL after `SecondaryCountryID`,
  add constraint `FK_chatroom_5` FOREIGN KEY (`LocationID`) REFERENCES `location` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT;


alter table `fusion`.`menu` 
  add column `CountryID` int(11) NULL after `URL`,
  add constraint `FK_menu_1` FOREIGN KEY (`CountryID`) REFERENCES `country` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT;


