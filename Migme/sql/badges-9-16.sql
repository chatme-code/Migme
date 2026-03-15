BEGIN;

INSERT INTO badge (ID, Name, Description) VALUES (9, 'Sturdy Thirty', 'Sturdy Thirty badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType) 
 VALUES ('Sturdy Thirty Badge Reward', 'Badge Reward for Sturdy Thirty', NULL, 25, 1, 30, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0); 
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Sturdy Thirty Badge Reward'), (select id from badge where name = 'Sturdy Thirty'));

INSERT INTO badge (ID, Name, Description) VALUES (10, 'Random Gifter', 'Random Gifter badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Random Gifter Badge Reward', 'Badge Reward for Random Gifter', NULL, 2, 1, 100, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Random Gifter Badge Reward'), (select id from badge where name = 'Random Gifter'));

INSERT INTO badge (ID, Name, Description) VALUES (11, 'Generous Trooper', 'Generous Trooper badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Generous Trooper Badge Reward', 'Badge Reward for Generous Trooper', NULL, 2, 1, 1000, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Generous Trooper Badge Reward'), (select id from badge where name = 'Generous Trooper'));

INSERT INTO badge (ID, Name, Description) VALUES (12, 'Big-ticket Gifter', 'Big-ticket Gifter badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Big-ticket Gifter Badge Reward', 'Badge Reward for Big-ticket Gifter', NULL, 2, 1, 10000, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0);
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Big-ticket Gifter Badge Reward'), (select id from badge where name = 'Big-ticket Gifter'));

INSERT INTO badge (ID, Name, Description) VALUES (13, 'Lucky Taker', 'Lucky Taker badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Lucky Taker Badge Reward', 'Badge Reward for Lucky Taker', NULL, 3, 1, 10, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0); 
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Lucky Taker Badge Reward'), (select id from badge where name = 'Lucky Taker'));

INSERT INTO badge (ID, Name, Description) VALUES (14, 'Freeloader', 'Freeloader badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Freeloader Badge Reward', 'Badge Reward for Freeloader', NULL, 3, 1, 100, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0); 
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Freeloader Badge Reward'), (select id from badge where name = 'Freeloader'));

INSERT INTO badge (ID, Name, Description) VALUES (15, 'High Roller', 'High Roller badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('High Roller Badge Reward', 'Badge Reward for High Roller', NULL, 3, 1, 1000, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0); 
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'High Roller Badge Reward'), (select id from badge where name = 'High Roller'));

INSERT INTO badge (ID, Name, Description) VALUES (16, 'Super Styler', 'Super Styler badge');
INSERT INTO rewardprogram (Name, Description, CountryID, Type, RewardFrequency, QuantityRequired, AmountRequired, AmountRequiredCurrency, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, IMNotification, EmailNotification, SMSNotification, StartDate, EndDate, Status, ItemRewardType, MaxMigLevel, MinMigLevel, userType)
 VALUES ('Super Styler Badge Reward', 'Badge Reward for Super Styler', NULL, 4, 1, 10, 0.00, 'USD', 0, 0, 0, 'USD', NULL, NULL, NULL, curdate(), date_add(curdate(), interval 999 year), 1, 1, NULL, NULL, 0); 
INSERT INTO BadgeReward (RewardProgramID, BadgeID) VALUES ((select id from rewardprogram where name = 'Super Styler Badge Reward'), (select id from badge where name = 'Super Styler'));

COMMIT;
