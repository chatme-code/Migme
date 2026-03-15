CREATE TABLE `discounttier` (
  `ID` int(11) NOT NULL,
  `Name` varchar(128) NOT NULL default '',
  `Type` int(11) NOT NULL default '0',
  `ActualMin` double(12,2) NOT NULL default '0.00',
  `DisplayMin` double(12,2) NOT NULL default '0.00',
  `Max` double(12,2) NOT NULL default '0.00',
  `Currency` varchar(6) NOT NULL default '',
  `PercentageDiscount` double(12,2) NOT NULL default '0.00',
  `ApplyToCreditCard` int(11) NOT NULL default '0',
  `ApplyToBankTransfer` int(11) NOT NULL default '0',
  `ApplyToWesternUnion` int(11) NOT NULL default '0',
  `ApplyToVoucher` int(11) NOT NULL default '0',
  `ApplyToTT` int(11) NOT NULL default '0',
  `Status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`),
  KEY `FK_DiscountTier_1` (`Currency`),
  CONSTRAINT `FK_DiscountTier_1` FOREIGN KEY (`Currency`) REFERENCES `currency` (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `discounttier` (`ID`, `Name`, `Type`, `ActualMin`, `DisplayMin`, `Max`, `Currency`, `PercentageDiscount`, `ApplyToCreditCard`, `ApplyToBankTransfer`, `ApplyToWesternUnion`, `ApplyToVoucher`, `ApplyToTT`, `Status`) values('1','30% Starter Discount','1','4.80','5.00','3250.00','USD','30.00','1','1','1','0','1','1');
insert into `discounttier` (`ID`, `Name`, `Type`, `ActualMin`, `DisplayMin`, `Max`, `Currency`, `PercentageDiscount`, `ApplyToCreditCard`, `ApplyToBankTransfer`, `ApplyToWesternUnion`, `ApplyToVoucher`, `ApplyToTT`, `Status`) values('2','30% Discount','2','65.00','70.00','3250.00','USD','30.00','1','1','1','0','1','1');
insert into `discounttier` (`ID`, `Name`, `Type`, `ActualMin`, `DisplayMin`, `Max`, `Currency`, `PercentageDiscount`, `ApplyToCreditCard`, `ApplyToBankTransfer`, `ApplyToWesternUnion`, `ApplyToVoucher`, `ApplyToTT`, `Status`) values('3','35% Discount','2','3200.00','3250.00','999999.00','USD','35.00','1','1','1','0','1','1');

CREATE TABLE `applieddiscount` (
  `ID` int(11) NOT NULL auto_increment,
  `Username` varchar(128) NOT NULL default '',
  `DateCreated` datetime NOT NULL default '0000-00-00 00:00:00',
  `DiscountTierID` int(11) NOT NULL default '0',
  `AccountEntryID` int(11) NOT NULL default '0',
  `Amount` double(12,2) NOT NULL default '0.00',
  `Currency` varchar(6) NOT NULL default '',
  `ExchangeRate` double(15,4) NOT NULL default '0.0000',
  PRIMARY KEY  (`ID`),
  KEY `FK_AppliedDiscount_1` (`Username`),
  KEY `FK_AppliedDiscount_2` (`DiscountTierID`),
  KEY `FK_AppliedDiscount_3` (`AccountEntryID`),
  KEY `FK_AppliedDiscount_4` (`Currency`),
  CONSTRAINT `FK_AppliedDiscount_1` FOREIGN KEY (`Username`) REFERENCES `user` (`Username`),
  CONSTRAINT `FK_AppliedDiscount_2` FOREIGN KEY (`DiscountTierID`) REFERENCES `discounttier` (`ID`),
  CONSTRAINT `FK_AppliedDiscount_3` FOREIGN KEY (`AccountEntryID`) REFERENCES `accountentry` (`ID`),
  CONSTRAINT `FK_AppliedDiscount_4` FOREIGN KEY (`Currency`) REFERENCES `currency` (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;