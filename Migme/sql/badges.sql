CREATE TABLE `Badge` (
	`ID` int(11) NOT NULL,
	`Name` varchar(128) NOT NULL,
	`Description` varchar(128) NOT NULL,

	PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `BadgeReward` (
	`RewardProgramID` int(11) NOT NULL,
	`BadgeID` int(11) NOT NULL,
	PRIMARY KEY (`RewardProgramID`, `BadgeID`),
	KEY `FK_BadgeReward_RewardProgramID` (`RewardProgramID`),
	KEY `FK_BadgeReward_BadgeID` (`BadgeID`),
	CONSTRAINT `FK_BadgeReward_RewardProgramID` FOREIGN KEY (`RewardProgramID`) REFERENCES `rewardprogram` (`ID`),
	CONSTRAINT `FK_BadgeReward_BadgeID` FOREIGN KEY (`BadgeID`) REFERENCES `badge` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `RewardCriteria` (
	`ID` int(11) NOT NULL AUTO_INCREMENT,
	`RewardProgramID` int(11) NOT NULL,
	`Type` int(11) NOT NULL,
	`RewardFrequency` int(11) NOT NULL,
	`QuantityRequired` int(11) NOT NULL,
	`AmountRequired` double(12,2) NOT NULL,
	`AmountRequiredCurrency` varchar(6) NOT NULL,
	PRIMARY KEY (`ID`),
	KEY `FK_RewardCriteria_RewardProgramID` (`RewardProgramID`),
	CONSTRAINT `FK_RewardCriteria_RewardProgramID` FOREIGN KEY (`RewardProgramID`) REFERENCES `rewardprogram` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `BadgeRewarded` (
	  `ID` int(11) NOT NULL AUTO_INCREMENT,
	  `RewardProgramCompletedID` int(11) NOT NULL,
	  `BadgeID` int(11) NOT NULL,
	  PRIMARY KEY (`ID`),
	  KEY `FK_BadgeRewarded_RewardProgramCompletedID` (`RewardProgramCompletedID`),
	  KEY `FK_BadgeRewarded_BadgeID` (`BadgeID`),
	  CONSTRAINT `FK_BadgeRewarded_RewardProgramCompletedID` FOREIGN KEY (`RewardProgramCompletedID`) REFERENCES `rewardprogramcompleted` (`ID`),
	  CONSTRAINT `FK_BadgeRewarded_BadgeID` FOREIGN KEY (`BadgeID`) REFERENCES `Badge` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- populate values
INSERT INTO badge (ID, Name, Description) VALUES (1, 'Adventurer', 'Adventurer for first migbo post');

-- badge reward, type==0, rewardfreq==0, criteria specified in rewardcriteria
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
  VALUES ('Adventurer Badge Reward', 'Badge reward for Adventurer', NULL, 24, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '9999-12-31', 1, 1, NULL, NULL);

INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Adventurer Badge Reward'), (select id from badge where name = 'Adventurer'));
INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
  VALUES ((select id from rewardprogram where name = 'Adventurer Badge Reward'), 24, 1, 1, 0, 'USD');


INSERT INTO badge (ID, Name, Description) VALUES (5, 'XLent migLover (silver)', 'XL badge silver');

-- badge reward, type==0, rewardfreq==0, criteria specified in rewardcriteria
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
  VALUES ('XLent migLover (silver) Badge Reward', 'Badge reward for XLent migLover (silver)', NULL, 24, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '1970-01-01', 1, 1, NULL, NULL);
--  VALUES ('Adventurer Badge Reward', 'Badge reward for Adventurer', NULL, 0, 0, 0, 0, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '9999-12-31', 1, 1, NULL, NULL);

INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'XLent migLover (silver) Badge Reward'), (select id from badge where name = 'XLent migLover (silver)'));
-- adventurer badge, 1 original migbo post, once-off
INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
  VALUES ((select id from rewardprogram where name = 'XLent migLover (silver) Badge Reward'), 24, 1, 1, 0, 'USD');

INSERT INTO badge (ID, Name, Description) VALUES (6, 'Coach', 'Coach badge');

-- badge reward, type==0, rewardfreq==0, criteria specified in rewardcriteria
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
  VALUES ('Coach Badge Reward', 'Badge reward for coach', NULL, 24, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '1970-01-01', 1, 1, NULL, NULL);
--  VALUES ('Adventurer Badge Reward', 'Badge reward for Adventurer', NULL, 0, 0, 0, 0, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '9999-12-31', 1, 1, NULL, NULL);

INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Coach Badge Reward'), (select id from badge where name = 'Coach'));
INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
  VALUES ((select id from rewardprogram where name = 'Coach Badge Reward'), 24, 1, 1, 0, 'USD');

INSERT INTO badge (ID, Name, Description) VALUES (7, 'Fan-tastique', 'Fan-tastique badge');

-- badge reward, type==0, rewardfreq==0, criteria specified in rewardcriteria
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
  VALUES ('Fan-tastique Badge Reward', 'Badge reward for Fan-tastique', NULL, 24, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '1970-01-01', 1, 1, NULL, NULL);

INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Fan-tastique Badge Reward'), (select id from badge where name = 'Fan-tastique'));
INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
  VALUES ((select id from rewardprogram where name = 'Fan-tastique Badge Reward'), 24, 1, 1, 0, 'USD');

INSERT INTO badge (ID, Name, Description) VALUES (8, 'Socialista', 'Socialista badge');

-- badge reward, type==0, rewardfreq==0, criteria specified in rewardcriteria
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
  VALUES ('Socialista Badge Reward', 'Badge reward for Socialista', NULL, 24, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '1970-01-01', 1, 1, NULL, NULL);

INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Socialista Badge Reward'), (select id from badge where name = 'Socialista'));
INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
  VALUES ((select id from rewardprogram where name = 'Socialista Badge Reward'), 24, 1, 1, 0, 'USD');

INSERT INTO badge (ID, Name, Description) VALUES (17, 'Nokia Gifter', 'Nokia Gifter badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('Nokia Gifter Badge Reward', 'Badge reward for Nokia Gifter', (select id from country where name='Indonesia'), 0, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '2012-06-01', '2012-06-15', 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Nokia Gifter Badge Reward'), (select id from badge where name = 'Nokia Gifter'));

INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
 VALUES ((select id from rewardprogram where name = 'Nokia Gifter Badge Reward'), 0, 1, 1, 0, 'USD');

INSERT INTO badge (ID, Name, Description) VALUES (18, 'EA CLUEDO Jr. Detective', 'EA CLUEDO Jr. Detective badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('EA CLUEDO Jr. Detective Badge Reward', 'Badge reward for EA CLUEDO Jr. Detective', NULL, 0, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '2012-06-20', '2012-06-30', 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'EA CLUEDO Jr. Detective Badge Reward'), (select id from badge where name = 'EA CLUEDO Jr. Detective'));

INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
 VALUES ((select id from rewardprogram where name = 'EA CLUEDO Jr. Detective Badge Reward'), 0, 1, 1, 0, 'USD');


INSERT INTO badge (ID, Name, Description) VALUES (34, 'Head Hunter', 'Head Hunter badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('Head Hunter Badge Reward', 'Badge reward for Head Hunter', NULL, 0, 1, 1, -1, 'USD', 0, 0, -1, 'USD', NULL, NULL, NULL, '1970-01-01', '9999-12-31', 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Head Hunter Badge Reward'), (select id from badge where name = 'Head Hunter'));

INSERT INTO RewardCriteria (RewardProgramID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency)
 VALUES ((select id from rewardprogram where name = 'Head Hunter Badge Reward'), 0, 1, 1, 0, 'USD');

