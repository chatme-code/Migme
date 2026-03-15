ALTER TABLE `emoticonpack`
    ADD COLUMN `ServiceID` INT(11) DEFAULT NULL AFTER `Price`,
    ADD COLUMN `SortOrder` INT(11) NULL AFTER `GroupVIPOnly`,
    ADD COLUMN `ForSale` INT(11) NOT NULL DEFAULT 1 AFTER `SortOrder`;

   alter table `fusion`.`emoticonpack` add constraint `FK_emoticonpack_1` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ID`);
    
INSERT INTO service (NAME, description, freetrialdays, durationdays, awardedcredit, awardedcreditcurrency, billingmethod, cost, costcurrency, billingconfirmationsms, expiryremindersms, STATUS)
VALUES ('Super emoticon pack', 'Cool 8-frame animated emoticons', 0, 30, 0, 'USD', 1, 0.25, 'USD', NULL, NULL, 1);

CREATE TABLE `textcolourpack` (
  `ID` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(128) NOT NULL,
  `ServiceID` INT NOT NULL,
  `GroupID` INT NULL,
  `GroupVIPOnly` BOOLEAN NULL,
  `SortOrder` INT NULL,
  `Status` INT NOT NULL DEFAULT 1,
  FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ID`),
  FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

CREATE TABLE `textcolour` (
  `TextColourPackID` INT NOT NULL,
  `RGB` VARCHAR(6) NOT NULL,
  FOREIGN KEY (`TextColourPackID`) REFERENCES `textcolourpack` (`ID`),
  PRIMARY KEY (`TextColourPackID`, `RGB`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

