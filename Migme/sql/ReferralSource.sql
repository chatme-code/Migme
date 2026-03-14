CREATE TABLE `referralsource` (
  `ID` SMALLINT unsigned NOT NULL auto_increment,
  `Code` VARCHAR(16) NOT NULL,
  `Description` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `idx_code`(`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `userreferralsource` (
  `UserID` int(10) unsigned NOT NULL,
  `ReferralSourceID` SMALLINT unsigned NOT NULL,
  PRIMARY KEY (`UserID`, `ReferralSourceID`),
  FOREIGN KEY (`ReferralSourceID`) REFERENCES `referralsource` (`ID`),
  FOREIGN KEY (`UserID`) REFERENCES `userid` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
