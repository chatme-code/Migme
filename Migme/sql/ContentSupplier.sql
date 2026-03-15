CREATE TABLE `contentsupplier` (
  `ID` int(11) NOT NULL auto_increment,
  `Name` varchar(128) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `content`
ADD COLUMN `ContentSupplierID` INT(11) NULL AFTER `ContentProviderID`,
ADD FOREIGN KEY (`ContentSupplierID`) REFERENCES `contentsupplier` (`ID`);

INSERT INTO contentsupplier (Name) VALUES
('Oplayo'),
('START MOBILE');

UPDATE content SET contentsupplierid = 1 WHERE ProviderID >= 53494 AND ProviderID <= 53551;
UPDATE content SET ContentSupplierID=2 WHERE ID >= 8613 AND ID <= 8771;
