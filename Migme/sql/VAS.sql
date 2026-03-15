

CREATE TABLE `fusion`.`AccountEntrySource` (
  `ID` INTEGER NOT NULL DEFAULT NULL AUTO_INCREMENT,
  `AccountEntryID` INTEGER NOT NULL,
  `IPAddress` VARCHAR(128),
  `SessionID` VARCHAR(128),
  `MobileDevice` VARCHAR(128),
  `UserAgent` VARCHAR(128),
  `IMEI` VARCHAR(128),
  `MerchantUserID` INTEGER(10) UNSIGNED,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_AccountEntrySource_1` FOREIGN KEY `FK_AccountEntrySource_1` (`AccountEntryID`)
    REFERENCES `accountentry` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_AccountEntrySource_2` FOREIGN KEY `FK_AccountEntrySource_2` (`MerchantUserID`)
    REFERENCES `userid` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;


CREATE TABLE `fusion`.`MerchantTag` (
  `ID` INTEGER NOT NULL DEFAULT NULL AUTO_INCREMENT,
  `UserID` INTEGER(10) UNSIGNED NOT NULL,
  `MerchantUserID` INTEGER(10) UNSIGNED NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  `LastSalesDate` DATETIME NOT NULL,
  `Status` INTEGER NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_MerchantTag_1` FOREIGN KEY `FK_MerchantTag_1` (`UserID`)
    REFERENCES `userid` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_MerchantTag_2` FOREIGN KEY `FK_MerchantTag_2` (`MerchantUserID`)
    REFERENCES `userid` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;


CREATE TABLE `fusion`.`RegistrationDevice` (
  `ID` INTEGER NOT NULL DEFAULT NULL AUTO_INCREMENT,
  `UserID` INTEGER(10) UNSIGNED,
  `MobileDevice` VARCHAR(128),
  `UserAgent` VARCHAR(128),
  `IMEI` VARCHAR(128),
  PRIMARY KEY (`ID`),
  CONSTRAINT `FK_RegistrationDevice_1` FOREIGN KEY `FK_RegistrationDevice_1` (`UserID`)
    REFERENCES `userid` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;
