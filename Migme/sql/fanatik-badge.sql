begin;
INSERT INTO rewardprogramprocessors (shortName, className, status) VALUES ('MigboCampaignRewardProgramProcessor', 'com.projectgoth.fusion.rewardsystem.processors.MigboCampaignRewardProgramProcessor', 1);
INSERT INTO rewardprogramprocessormapping (programType, sequence, processorID) select 41, 1, id from rewardprogramprocessors where shortName='MigboCampaignRewardProgramProcessor';

INSERT INTO badge (ID, Name, Description) VALUES (52, 'FANATIK Badge', 'FANATIK Badge badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('FANATIK Badge Badge Reward', 'Badge reward for FANATIK Badge', NULL, 41, 1, 1, 0, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, '2012-11-15', '2012-12-14', 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'FANATIK Badge Badge Reward'), (select id from badge where name = 'FANATIK Badge'));

INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='FANATIK Badge Badge Reward'), 'campaignID', 3);
INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='FANATIK Badge Badge Reward'), 'eventType', 'registration');

INSERT INTO badge (ID, Name, Description) VALUES (53, 'FANATIK Voter Badge', 'FANATIK Voter Badge badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('FANATIK Voter Badge Badge Reward', 'Badge reward for FANATIK Voter Badge', NULL, 41, 1, 1, 0, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, '2012-11-15', '2012-12-14', 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'FANATIK Voter Badge Badge Reward'), (select id from badge where name = 'FANATIK Voter Badge'));

INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='FANATIK Voter Badge Badge Reward'), 'campaignID', 3);
INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='FANATIK Voter Badge Badge Reward'), 'eventType', 'tag_created');
INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='FANATIK Voter Badge Badge Reward'), 'entityType', 'PO');

commit;
