CREATE TABLE `fusion`.`UserSetting` (
  `Username` VARCHAR(128) NOT NULL,
  `Type` INTEGER NOT NULL,
  `Value` INTEGER NOT NULL,
  PRIMARY KEY (`Username`, `Type`),
  CONSTRAINT `FK_UserSetting_1` FOREIGN KEY `FK_UserSetting_1` (`Username`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;
