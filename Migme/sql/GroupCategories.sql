CREATE TABLE `groupcategory` (
  `ID` int(11) NOT NULL auto_increment,
  `Name` varchar(128) NOT NULL,
  `SortOrder` int(11) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB;

insert into groupcategory(name, sortorder) values
('Entertainment',1),
('Sports',2),
('Politics',3);

ALTER TABLE `groups` ADD COLUMN `GroupCategoryID` INT(11) NULL AFTER `ReferralSMS`;
ALTER TABLE `groups` ADD CONSTRAINT `FK_GroupCategoryID` FOREIGN KEY (`GroupCategoryID`) REFERENCES `groupcategory` (`ID`);
ALTER TABLE `groups` ADD COLUMN `SortOrder` INT(11) NULL AFTER `GroupCategoryID`;

update groups set groupcategoryid=3 where id >= 1 and id <= 6;
update groups set groupcategoryid=2 where id >= 7 and id <= 15;
update groups set groupcategoryid=1 where id >= 16 and id <= 20;
