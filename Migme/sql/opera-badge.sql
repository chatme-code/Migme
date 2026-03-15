begin;
INSERT INTO rewardprogramprocessors (shortName, className, status) VALUES ('UserFirstAuthenticatedRewardProgramProcessor', 'com.projectgoth.fusion.rewardsystem.processors.UserFirstAuthenticatedRewardProgramProcessor', 1);
INSERT INTO rewardprogramprocessormapping (programType, sequence, processorID) select 42, 1, id from rewardprogramprocessors where shortName='UserFirstAuthenticatedRewardProgramProcessor';

INSERT INTO badge (ID, Name, Description) VALUES (54, 'Opera', 'Opera badge');

INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel)
 VALUES ('Opera Badge Reward', 'Badge reward for Opera Badge', NULL, 42, 1, 1, 0, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, '2012-11-15', NULL, 1, 1, NULL, NULL);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Opera Badge Reward'), (select id from badge where name = 'Opera'));

INSERT INTO rewardprogramparameters (rewardprogramID, ParamName, ParamValue)
 VALUES ((SELECT id FROM rewardprogram WHERE name='Opera Badge Reward'), 'campaignRegex', "opera_share|operashare|opera_feed|operafeed");

commit;
