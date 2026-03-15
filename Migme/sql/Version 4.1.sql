
-- System table
INSERT INTO system (propertyname, propertyvalue) VALUES ('UserNotificationURL', 'http://www.mig33.com/midlet/member/view_profile.php?username=');


-- ClientText table
INSERT INTO clienttext (id, TYPE, description, TEXT) VALUES (47, 2, 'Info text - User notification', 'You have %n pending contact request(s)');


-- Emoticon table
ALTER TABLE `fusion`.`emoticon` ADD COLUMN `Type` INT(11) NOT NULL AFTER `EmoticonPackID`;

UPDATE emoticon SET TYPE = 1;

-- ChatRoomBookmark table
CREATE TABLE `fusion`.`ChatRoomBookmark` (
  `Username` VARCHAR(128) NOT NULL,
  `ChatRoomName` VARCHAR(128) NOT NULL,
  `DateCreated` DATETIME NOT NULL,
  PRIMARY KEY (`Username`, `ChatRoomName`),
  INDEX `idx_ChatRoom_1`(`DateCreated`),
  CONSTRAINT `FK_ChatRoomBookmark_1` FOREIGN KEY `FK_ChatRoomBookmark_1` (`Username`)
    REFERENCES `user` (`Username`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `FK_ChatRoomBookmark_2` FOREIGN KEY `FK_ChatRoomBookmark_2` (`ChatRoomName`)
    REFERENCES `chatroom` (`Name`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = INNODB;