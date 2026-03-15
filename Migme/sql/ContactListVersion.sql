CREATE TABLE `fusion`.`ContactListVersion` (
  `UserID` INTEGER NOT NULL,
  `Version` INTEGER NOT NULL,
  PRIMARY KEY (`UserID`),
  CONSTRAINT `FK_ContactListVersion_1` FOREIGN KEY `FK_ContactListVersion_1` (`UserID`)
    REFERENCES `userid` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)