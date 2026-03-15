DROP TABLE  fusion.bot;

CREATE TABLE `fusion`.`bot`
(
   ID int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   Game varchar(20) NOT NULL Default ' ',
   DisplayName varchar(20) NOT NULL,
   Description varchar(128),
   CommandName varchar(128),
   ExecutableFileName varchar(128),
   LibraryPaths varchar(400),
   Type int DEFAULT 1 NOT NULL,
   Status int DEFAULT 0 NOT NULL,
   Leaderboards bit DEFAULT 0 NOT NULL,
   EmoticonKeyList varchar(128)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Bots
DELETE FROM bot;
INSERT INTO `fusion`.`bot` (ID,Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (1,'Werewolf','WerewolfBot','Find the werewolf before it kills all! Pay to enter the pot','wolf','com.projectgoth.fusion.botservice.bot.migbot.werewolf.Werewolf','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);
INSERT INTO `fusion`.`bot` (ID,Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (2,'Russian Roulette','RussianRoulette','Spin to see if you''re lucky.Click, you''re safe. BANG you''re out. Pay to enter the pot','rr','com.projectgoth.fusion.botservice.bot.migbot.russianroulette.RussianRoulette','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,1,';)');
INSERT INTO `fusion`.`bot` (ID,Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (3,'Trivia','TriviaBot','Challenge yourself or play with friends!','trivia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

-- WINDOWS version
/*
INSERT INTO `fusion`.`bot` (ID,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (1,'WerewolfBot','Find the werewolf before it kills all! Pay to enter the pot','wolf','com.projectgoth.fusion.botservice.bot.migbot.werewolf.Werewolf','C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,1,null);
INSERT INTO `fusion`.`bot` (ID,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (2,'RussianRoulette','Spin to see if you''re lucky.Click, you''re safe. BANG you''re out. Pay to enter the pot','roul','com.projectgoth.fusion.botservice.bot.migbot.russianroulette.RussianRoulette','C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,1,';)');
INSERT INTO `fusion`.`bot` (ID,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES (3,'TriviaBot','Questions and answers: Pay to start, free to answer!','trivia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,1,null);
*/

/* Alter existing table

ALTER TABLE bot ADD COLUMN Game varchar(20) NOT NULL Default ' ' AFTER `ID`;;
UPDATE bot SET Game = 'Trivia' where commandName = 'trivia';
UPDATE bot SET Game = 'Russian Roulette' where commandName = 'rr';
UPDATE bot SET Game = 'Werewolf' where commandName = 'wolf';
*/
-- Stores a row for each config parameter for a bot, such as billing amounts, timers, min/max number of players
DROP TABLE fusion.botconfig;
CREATE TABLE `fusion`.`botconfig`
(
   BotID int NOT NULL,   
   PropertyName varchar(30),
   PropertyValue varchar(128),
   Description varchar(128)   
);

ALTER TABLE botconfig ADD INDEX (BotID);
ALTER TABLE botconfig ADD CONSTRAINT FK_botconfig_bot FOREIGN KEY(BotID) REFERENCES bot(ID);

DELETE FROM botconfig;

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','0' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','45' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','5' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'TriviaBot';

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountJoinPot','5' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','90' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerSpin','20' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','2' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'RussianRoulette';

DELETE FROM botconfig WHERE botid = ?;
DELETE FROM botmessage WHERE botid = ?;

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'dayTime','45' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'nightTime','60' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'voteTime','30' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'tieGame','on' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountJoinPot','5' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','90' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','5' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'maxPlayers','12' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'WerewolfBot';

-- Stores a row for each message key and language code, for a bot
DROP TABLE fusion.botmessage;
CREATE TABLE `fusion`.`botmessage`
(
   BotID int DEFAULT 0,   
   MessageKey varchar(30),
   LanguageCode varchar(3),
   MessageValue varchar(128)   
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--ALTER TABLE botmessage ADD INDEX (BotID);
--ALTER TABLE botmessage ADD CONSTRAINT FK_botmessage_bot FOREIGN KEY(BotID) REFERENCES bot(ID);

ALTER TABLE botmessage ADD INDEX (LanguageCode);
ALTER TABLE botmessage ADD CONSTRAINT FK_botmessage_lang FOREIGN KEY(LanguageCode) REFERENCES language(Code);

ALTER TABLE `fusion`.`botmessage` ADD UNIQUE KEY (BotID, MessageKey, LanguageCode);

DELETE FROM botMessage;

-- Common to ALL bots (note that the botId = 0 for these rows)
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'ADDED_TO_GAME','ENG','PLAYER: added to game. ' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'CHARGES_APPLY_POT','ENG','Charges apply. CURRENCY AMOUNT_POT' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'CHARGES_APPLY_JOIN','ENG','Charges apply. CURRENCY AMOUNT_JOIN' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'CHARGE_NEW_GAME','ENG','PLAYER: Charges apply. CURRENCY AMOUNT_START for new game.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'CHARGE_NEW_POT','ENG','PLAYER: Charges apply. CURRENCY AMOUNT_POT Create/enter pot.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'CHARGE_CONF_NO_MSG','ENG',' CMD_NO to cancel. CONF_TIMER seconds' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'STARTGAME-NOTICE','ENG','PLAYER started a game!' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'INSUFFICIENT_FUNDS_START','ENG','PLAYER: Sorry, insufficient funds to start game.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'INSUFFICIENT_FUNDS_JOIN','ENG','PLAYER: Sorry, insufficient funds to join game.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'INSUFFICIENT_FUNDS_POT','ENG','PLAYER: Sorry, insufficient funds to join pot.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'STATUS-STARTED','ENG','A game is already started.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'STATUS-PLAYING','ENG','A game is currently on.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'STATUS-JOINING','ENG','A game is on. CMD_JOIN to join. Charges may apply.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'STATUS-CANNOT-START','ENG','Sorry, new game cannot be started now.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'PLAYER_CHARGED_START','ENG','PLAYER: You were charged LOCAL_CURNCY AMT_START_LOCAL to start game.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'PLAYER_CHARGED_JOIN','ENG','PLAYER: You were charged LOCAL_CURNCY AMT_JOIN_LOCAL to join game.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'PLAYER_CHARGED_POT','ENG','PLAYER: You were charged LOCAL_CURNCY AMT_POT_LOCAL to enter the pot.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'BOT_ADDED','ENG','Bot BOTNAME added to room.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'NOT_CHARGED','ENG','PLAYER: You were not charged.' ;
INSERT INTO `fusion`.`botmessage` (MessageKey,LanguageCode,MessageValue) SELECT 'INVALID_COMMAND','ENG','PLAYER: Invalid command.' ;

-- Russian Roulette
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CLICK','ENG','PLAYER : CLICK. You''re safe this time! ;)' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'BANG','ENG','PLAYER: BANG! Your luck ran out! OUT of the game.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP_AUTO_SPIN','ENG','PLAYER: Time''s up! Bot spins for you!' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','Russian Roulette is on now. Last player standing wins all!' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play Russian Roulette: CMD_START. Need MINPLAYERS players.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play now: CMD_START to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, CMD_START <entry_amount> ' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_PAID','ENG','Russian Roulette started. CMD_JOIN to join. Cost CURRENCY AMOUNT_POT. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_FREE','ENG','Russian Roulette started. CMD_JOIN to join. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Russian Roulette begins! TIMER_SPIN seconds each to spin.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_TURN_TO_SPIN','ENG','PLAYER: Your turn now.  SPIN_COMMAND to spin. TIMER_SPIN seconds.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_YOUR_TURN','ENG','PLAYER: It''s not your turn. ' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_PAID','ENG','Game over! LEADER WINS CURRENCY WINNINGS!! CONGRATS!' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_FREE','ENG','Game over! LEADER wins!! CONGRATS!' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_SPINS','ENG','PLAYER spins...' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'BOT_SPINS','ENG','Bot spins for PLAYER...' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','PLAYER: ERROR_INPUT invalid. Game not started.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NEXT_ROUND','ENG','Safe players, congrats! Get ready for ROUND #ROUND_NUMBER!' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_ENDED','ENG','PLAYER: Sorry, a game has already started.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You are already added to game.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SPIN_ORDER','ENG','Order is: PLAYERS' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the game.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_CANCELED','ENG','Billing error. Game canceled. No charges' FROM bot b WHERE displayName = 'RussianRoulette';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LEFT','ENG','PLAYER left the game. Didn''t feel lucky, eh?' FROM bot b WHERE displayName = 'RussianRoulette';

-- Werewolf

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'1-WOLF','ENG','Werewolf' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'MANY-WOLVES','ENG','Werewolves' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-WOLF','ENG','Werewolf' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-SEER','ENG','Seer' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-VILLAGER','ENG','Villager' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'STARTGAME_PAID','ENG','PLAYER started a game of Werewolf. CMD_JOIN to join. Cost CURRENCY AMOUNT_POT. TIME seconds' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'STARTGAME_FREE','ENG','PLAYER started a game of Werewolf. CMD_JOIN to join. TIME seconds' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ADDED','ENG','PLAYER: Added to game. ' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME-STARTED','ENG','Game  started! CMD_JOIN to join!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME-PLAYING','ENG','Sorry, game already going on' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-ENOUGH','ENG','Sorry, not enough people to start.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ADD-PRIORITY','ENG','Adding priority players...' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TWOWOLVES','ENG','THERE ARE 2 WOLVES IN THIS GAME.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DAYCHANGE','ENG','Duration of the day now TIME seconds.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHTCHANGE','ENG','Duration of the night now TIME seconds.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTECHANGE','ENG','Duration of the Lynch Vote now TIME seconds.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the hunt.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE','ENG','PLAYER fled.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-VILLAGER','ENG','PLAYER fled. They were a Villager.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-WOLF','ENG','PLAYER fled. They were a Wolf.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY','ENG','PLAYER fled. PLAYR2 has taken his place.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY-NOTICE-PLYR','ENG','PLAYER: PLAYER left the game. You take their place.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY-NOTICE-ALL','ENG','PLAYER left the game. PLAYR2 takes their place.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DAYTIME','ENG','Everyone, TIME seconds to discuss suspicions.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTETIME','ENG','Everyone, TIME secs to type ''!v <player>'' to lynch someone!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'HAS-VOTED','ENG','PLAYER voted for PLAYR2!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-VOTES','ENG','Nobody voted! ' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRSTNIGHT','ENG','Night - the wolf creeps…' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHTTIME','ENG','Moon rises, mob sleeps, but something stirs' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF-INSTRUCTIONS','ENG','WOLF, TIME secs to type ''!k <player>'' and kill a villager.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-INSTRUCTIONS','ENG','WOLF, TIME secs to decide who to kill. Type ''!k <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-INSTRUCTIONS','ENG','Seer, TIME secs to ask the spirits. Type ''!s <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF-CHOICE','ENG','PLAYER: You picked PLAYR2 to eat tonight.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-CHOICE','ENG','PLAYER: You picked PLAYR2. Who will the other one choose?' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-CHOICE-OTHER1','ENG','PLAYER chose to kill PLAYR2.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-CHOICE-OTHER2','ENG','PLAYR2 is PLAYER''s choice.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VILLAGER-ROLE','ENG','PLAYER: You''re a VILLAGER! Protect your village! Find the WOLF! Vote at daily Lynch Vote. Good luck!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'V-ROLE','ENG','PLAYER: You are a Villager.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF-ROLE','ENG','PLAYER: You''re the WOLF! By day, trick Villagers. By night, kill them! Shhhh…' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'W-ROLE','ENG','PLAYER: You are a Werewolf.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-ROLE','ENG','PLAYER: You and PLAYR2 are the WEREWOLVES. By day, trick Villagers. By night, kill them! Shhhh…' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WS-ROLE','ENG','PLAYER: You are a Werewolf. Other wolf is PLAYR2.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-ROLE','ENG','PLAYER: You''re a SEER! At night you can find the true face of a villager. But, reveal too much and the wolf will kill you!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'S-ROLE','ENG','PLAYER: You are the Seer.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LYNCH-LEFT','ENG','The guilty party left, no lynching.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VILLAGER-LYNCH','ENG','PLAYER is dragged to the stake and burned. Cheers fade as all realize PLAYER wasn''t a werewolf.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF-LYNCH','ENG','PLAYER is hung from a tree. As PLAYER is dying, fur sprouts.  A gunshot cracks as a villager puts a bullet in the beast''s head!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-LYNCH','ENG','PLAYER is tied to a log & dumped in water.  As Tarot cards float to the surface, all realize the error' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'IS-LYNCHED','ENG','PLAYER, the ROLE, is lynched!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-LYNCH','ENG','As night falls,  villagers can''t decide.  All take shelter.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VILLAGER-KILL','ENG','Morning: villagers can''t find PLAYER.  After a scream, PLAYER is found hanging from a tree.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-KILL','ENG','On the road, a bloody Ouija Board, atop it sits PLAYER''s head. PLAYER was the Seer.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'IS-KILLED','ENG','PLAYER, the ROLE, has been killed!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-KILL','ENG','Morning, and relief.  No attack this night.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-VOTED','ENG','PLAYER grabs chest, drops.  A lesson for defenders of the wolf.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-VOTED-NOTICE','ENG','PLAYER: You''re kicked for not voting twice in a row.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF-WIN','ENG','Having fooled the rest, PLAYER the Werewolf, kills the last.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-WIN','ENG','That night, the Wolves eat the rest. ' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VILLAGERS-WIN','ENG','With the beasts slain,  villagers cheer! Free from the WOLF!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-VILL','ENG','Congratulations, Villagers! You win!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-WOLF','ENG','Congratulations, PLAYER! You win!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-WOLVES','ENG','Congratulations, Werewolves! You win!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLVES-WERE','ENG','The Werewolves were: ' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-DEAD','ENG','PLAYER: You''re dead' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-WOLF','ENG','PLAYER: You are not a Werewolf!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WILL-SEE','ENG','PLAYER: You will see the identity of PLAYR2 in the morning' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-SEER','ENG','PLAYER: You''re not the Seer!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-SEE','ENG','PLAYER: PLAYR2 is ISAWOLF?!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-SEE-KILLED','ENG','PLAYER: The WOLF got to you before your vision did...' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEER-SEE-TARGET-KILLED','ENG','PLAYER: You didn''t need the spirits -  your target was also that of the WOLF' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TALLY','ENG','Tallying Votes...' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIE','ENG','A tie. Randomly choosing one...' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DYING-BREATH','ENG','PLAYER: You''re allowed a single line as your dying breath.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'COULD-NOT-ADD','ENG','PLAYER: could not add you. Sorry, please try again.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'MAX-REACHED','ENG','PLAYER: Sorry, maximum players reached. Please wait for the next game.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PRIORITY-LIST-ERROR','ENG','PLAYER: Could not add you to priority list. Please try again.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALL-LISTS-FULL','ENG','PLAYER: Sorry, both player and priority lists are full. Please wait for the next game.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You are already in this game.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_DEAD','ENG','PLAYER: Your choice is already dead!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DEAD_CANT_VOTE','ENG','PLAYER: You''re dead. You can''t vote!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CHOICE','ENG','PLAYER: Invalid choice.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_VOTE_COMMAND','ENG','PLAYER: Please vote in the valid format ''!v <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_KILL_COMMAND','ENG','PLAYER: Please kill in the valid format ''!k <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_SEE_COMMAND','ENG','PLAYER: Please see in the valid format ''!s <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_VOTED','ENG','PLAYER: You already voted.Vote again tomorrow.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CANT_EAT_SELF','ENG','PLAYER: You cannot eat yourself!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VALID_PLAYER','ENG','PLAYER: Please choose  a valid player.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_PLAYING','ENG','PLAYER: You''re not playing.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_KNOW_HUMAN','ENG','PLAYER: You already know you''re human!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYERS_ALIVE','ENG','Alive - PLAYER' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WOLF_SELECTION_LEFT','ENG','PLAYER: The person you selected has left the game.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LEFT_PRIO_LIST','ENG','A player on the priority list left.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play Werewolf: !start. Need MINPLAYERS players.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play now: !start to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, !start <entry_amount> ' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_ENDED','ENG','Joining ends…' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_PAID','ENG','Game over! WINNER_STRING WINNINGS_TEXT' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_FREE','ENG','Game over! CONGRATS!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_CANCELED','ENG','Billing error. Game canceled. No charges' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CANT_KILL_OTHER_WOLF','ENG','PLAYER: You cannot kill the other wolf PLAYR2' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','PLAYER: ERROR_INPUT invalid. Game not started.' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WINNINGS_NON_ZERO','ENG','CURRENCY WINNINGS!!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WINNINGS_ZERO','ENG','by default. But everyone died and noone won the pot. Sorry!' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTING_TIME_ONLY','ENG','PLAYER: Invalid command. Day time is for the lynch vote. Use ''!v <player>''' FROM bot b WHERE displayName = 'WerewolfBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHT_TIME_ONLY','ENG','PLAYER: Invalid command. Only wolves or seer can act at night.' FROM bot b WHERE displayName = 'WerewolfBot';

-- UNO emoticons
insert into emoticonpack values( 28, 2, 'UNO', null, 99999, NULL, NULL, null, null, 0, 1);

insert into emoticon values( 787, 28, 1, 'uno_blue', 12, 12, '/usr/fusion/emoticons/uno/uno_blue_12x12.gif', '/usr/fusion/emoticons/uno/uno_blue_12x12.png' );
insert into emoticon values( 788, 28, 1, 'uno_blue', 14, 14, '/usr/fusion/emoticons/uno/uno_blue_14x14.gif', '/usr/fusion/emoticons/uno/uno_blue_14x14.png' );
insert into emoticon values( 789, 28, 1, 'uno_blue', 16, 16, '/usr/fusion/emoticons/uno/uno_blue_16x16.gif', '/usr/fusion/emoticons/uno/uno_blue_16x16.png' );
insert into emoticon values( 790, 28, 1, 'uno_green', 12, 12, '/usr/fusion/emoticons/uno/uno_green_12x12.gif', '/usr/fusion/emoticons/uno/uno_green_12x12.png' );
insert into emoticon values( 791, 28, 1, 'uno_green', 14, 14, '/usr/fusion/emoticons/uno/uno_green_14x14.gif', '/usr/fusion/emoticons/uno/uno_green_14x14.png' );
insert into emoticon values( 792, 28, 1, 'uno_green', 16, 16, '/usr/fusion/emoticons/uno/uno_green_16x16.gif', '/usr/fusion/emoticons/uno/uno_green_16x16.png' );
insert into emoticon values( 793, 28, 1, 'uno_red', 12, 12, '/usr/fusion/emoticons/uno/uno_red_12x12.gif', '/usr/fusion/emoticons/uno/uno_red_12x12.png' );
insert into emoticon values( 794, 28, 1, 'uno_red', 14, 14, '/usr/fusion/emoticons/uno/uno_red_14x14.gif', '/usr/fusion/emoticons/uno/uno_red_14x14.png' );
insert into emoticon values( 795, 28, 1, 'uno_red', 16, 16, '/usr/fusion/emoticons/uno/uno_red_16x16.gif', '/usr/fusion/emoticons/uno/uno_red_16x16.png' );
insert into emoticon values( 796, 28, 1, 'uno_yellow', 12, 12, '/usr/fusion/emoticons/uno/uno_yellow_12x12.gif', '/usr/fusion/emoticons/uno/uno_yellow_12x12.png' );
insert into emoticon values( 797, 28, 1, 'uno_yellow', 14, 14, '/usr/fusion/emoticons/uno/uno_yellow_14x14.gif', '/usr/fusion/emoticons/uno/uno_yellow_14x14.png' );
insert into emoticon values( 798, 28, 1, 'uno_yellow', 16, 16, '/usr/fusion/emoticons/uno/uno_yellow_16x16.gif', '/usr/fusion/emoticons/uno/uno_yellow_16x16.png' );

insert into emoticonhotkey (emoticonid, type, hotkey) values( 787, 1, '(uno_blue)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 788, 1, '(uno_blue)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 789, 1, '(uno_blue)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 790, 1, '(uno_green)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 791, 1, '(uno_green)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 792, 1, '(uno_green)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 793, 1, '(uno_red)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 794, 1, '(uno_red)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 795, 1, '(uno_red)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 796, 1, '(uno_yellow)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 797, 1, '(uno_yellow)' );
insert into emoticonhotkey (emoticonid, type, hotkey) values( 798, 1, '(uno_yellow)' );


-- Tables for betting pot
CREATE TABLE `fusion`.`pot`
(
   `ID` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `BotID` int NOT NULL,
   `BotInstanceID` varchar(128) NOT NULL,
   `DateCreated` timestamp,
   `DatePaidOut` timestamp NULL,
   `RakeAmount` double(12,2),
   `RakeFundedAmount` double(12,2),
   `RakePercent` double(5,2),
   `Status` int,
   FOREIGN KEY (`BotID`) REFERENCES `bot` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `fusion`.`potstake`
(
   `ID` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `PotID` INT NOT NULL,
   `UserID` INT(10) UNSIGNED NOT NULL,
   `DateCreated` TIMESTAMP,
   `Amount` DOUBLE(12,2),
   `FundedAmount` DOUBLE(12,2),
   `Currency` VARCHAR(3),
   `ExchangeRate` DOUBLE(15,4),
   `Eligible` BOOLEAN NOT NULL DEFAULT 1,
   FOREIGN KEY (`PotID`) REFERENCES `pot` (`ID`),
   FOREIGN KEY (`UserID`) REFERENCES `userid` (`ID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

ALTER TABLE chatroom ADD COLUMN `AllowBots` BOOLEAN NOT NULL DEFAULT 0 AFTER `AllowUserKeywords`;

insert into chatroom (name, type, creator, groupid, adultonly, allowKicking, allowbots, maximumsize, datecreated, status)
values 
('Game 1', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 2', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 3', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 4', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 5', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 6', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 7', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 8', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 9', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 10', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 11', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 12', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 13', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 14', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 15', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 16', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 17', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 18', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 19', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 20', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 21', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 22', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 23', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 24', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 25', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 26', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 27', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 28', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 29', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 30', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 31', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 32', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 33', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 34', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 35', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 36', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 37', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 38', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 39', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 40', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 41', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 42', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 43', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 44', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 45', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 46', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 47', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 48', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 49', 1, 'mig33', null, 0, 0, 1, 25, now(), 1),
('Game 50', 1, 'mig33', null, 0, 0, 1, 25, now(), 1);


-- update message text
UPDATE botmessage set messageValue = 'PLAYER spins...' WHERE messageKey = 'PLAYER_SPINS' and languageCode = 'ENG';
UPDATE botmessage set messageValue = 'Bot spins for PLAYER...' WHERE messageKey = 'BOT_SPINS' and languageCode = 'ENG';

-- create new bot 'Vampire'
INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Vampire','VampireBot','Find the vampire before it kills all! Pay to enter the pot','vamp','com.projectgoth.fusion.botservice.bot.migbot.vampire.Vampire','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'dayTime','45' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'nightTime','60' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'voteTime','30' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'tieGame','on' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountJoinPot','5' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','90' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','5' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'maxPlayers','12' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'VampireBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'1-VAMPIRE','ENG','Vampire' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'MANY-VAMPIRES','ENG','Vampires' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-VAMPIRE','ENG','Vampire' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-SLAYER','ENG','Slayer' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROLE-EXPLORER','ENG','Explorer' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'STARTGAME_PAID','ENG','PLAYER started a game of Vampire. CMD_JOIN to join. Cost CURRENCY AMOUNT_POT. TIME seconds' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'STARTGAME_FREE','ENG','PLAYER started a game of Vampire. CMD_JOIN to join. TIME seconds' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ADDED','ENG','PLAYER: Added to game. ' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME-STARTED','ENG','Game  started! CMD_JOIN to join!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME-PLAYING','ENG','Sorry, game already going on' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-ENOUGH','ENG','Sorry, not enough people to start.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ADD-PRIORITY','ENG','Adding priority players...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TWOVAMPIRES','ENG','THERE ARE 2 VAMPIRES IN THIS GAME.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DAYCHANGE','ENG','Duration of the day now TIME seconds.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHTCHANGE','ENG','Duration of the night now TIME seconds.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTECHANGE','ENG','Duration of the Lynch Vote now TIME seconds.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the hunt.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE','ENG','PLAYER fled.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-EXPLORER','ENG','PLAYER fled. They were an Explorer.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-VAMPIRE','ENG','PLAYER fled. They were a Vampire.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY','ENG','PLAYER fled. PLAYR2 has taken his place.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY-NOTICE-PLYR','ENG','PLAYER: PLAYER left the game. You take their place.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FLEE-PRIORITY-NOTICE-ALL','ENG','PLAYER left the game. PLAYR2 takes their place.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DAYTIME','ENG','Everyone, TIME seconds to discuss suspicions.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTETIME','ENG','Everyone, TIME secs to type ''!v <player>'' to lynch someone!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'HAS-VOTED','ENG','PLAYER voted for PLAYR2!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-VOTES','ENG','Nobody voted! ' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRSTNIGHT','ENG','Night - the Vampire creeps...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHTTIME','ENG','Moon rises, mob sleeps, but something stirs' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE-INSTRUCTIONS','ENG','VAMPIRE, TIME secs to type ''!k <player>'' and kill an explorer.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-INSTRUCTIONS','ENG','VAMPIRE, TIME secs to decide who to kill. Type ''!k <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-INSTRUCTIONS','ENG','Slayer, TIME secs to ask the spirits. Type ''!s <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE-CHOICE','ENG','PLAYER: You picked PLAYR2 to eat tonight.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-CHOICE','ENG','PLAYER: You picked PLAYR2. Who will the other one choose?' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-CHOICE-OTHER1','ENG','PLAYER chose to kill PLAYR2.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-CHOICE-OTHER2','ENG','PLAYR2 is PLAYER''s choice.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'EXPLORER-ROLE','ENG','PLAYER: You''re an EXPLORER! Protect your village! Find the VAMPIRE! Vote at daily Lynch Vote. Good luck!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'E-ROLE','ENG','PLAYER: You are an Explorer.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE-ROLE','ENG','PLAYER: You''re the VAMPIRE! By day, trick Explorers. By night, kill them! Shhhh...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'V-ROLE','ENG','PLAYER: You are a Vampire.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-ROLE','ENG','PLAYER: You and PLAYR2 are the VAMPIRES. By day, trick Explorers. By night, kill them! Shhhh...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VS-ROLE','ENG','PLAYER: You are a Vampire. Other Vampire is PLAYR2.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-ROLE','ENG','PLAYER: You''re a SLAYER! At night you can find the true face of an explorer. But, reveal too much and the Vampire will kill you!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'S-ROLE','ENG','PLAYER: You are the Slayer.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LYNCH-LEFT','ENG','The guilty party left, no lynching.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'EXPLORER-LYNCH','ENG','PLAYER is dragged to the stake and burned. Cheers fade as all realize PLAYER wasn''t a Vampire.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE-LYNCH','ENG','PLAYER is dragged to a tree and hung. As PLAYER is dying, fangs grow. The demon yells as an Explorer stakes it through the heart with a sharpened piece of wood…' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-LYNCH','ENG','PLAYER is tied to a log & dumped in water.  As Tarot cards float to the surface, all realize the error' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'IS-LYNCHED','ENG','PLAYER, the ROLE, is lynched!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-LYNCH','ENG','As night falls,  explorers can''t decide.  All take shelter.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'EXPLORER-KILL','ENG','Morning: explorers can''t find PLAYER.  After a scream, PLAYER is found hanging from a tree.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-KILL','ENG','On the road, a bloody Ouija Board, atop it sits PLAYER''s head. PLAYER was the Slayer.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'IS-KILLED','ENG','PLAYER, the ROLE, has been killed!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO-KILL','ENG','Morning, and relief.  No attack this night.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-VOTED','ENG','PLAYER grabs chest, drops.  A lesson for defenders of the Vampire.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-VOTED-NOTICE','ENG','PLAYER: You''re kicked for not voting twice in a row.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE-WIN','ENG','Having fooled the rest, PLAYER the Vampire, kills the last.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-WIN','ENG','That night, the Vampires eat the rest. ' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'EXPLORERS-WIN','ENG','With the demons slain,  explorers cheer! Free from the VAMPIRE!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-VILL','ENG','Congratulations, Explorers! You win!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-VAMPIRE','ENG','Congratulations, PLAYER! You win!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CONGR-VAMPIRES','ENG','Congratulations, Vampires! You win!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRES-WERE','ENG','The Vampires were: ' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-DEAD','ENG','PLAYER: You''re dead' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-VAMPIRE','ENG','PLAYER: You are not a Vampire!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WILL-SEE','ENG','PLAYER: You will see the identity of PLAYR2 in the morning' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT-SLAYER','ENG','PLAYER: You''re not the Slayer!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-SEE','ENG','PLAYER: PLAYR2 is ISAVAMPIRE?!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-SEE-KILLED','ENG','PLAYER: The VAMPIRE got to you before your vision did...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SLAYER-SEE-TARGET-KILLED','ENG','PLAYER: You didn''t need the spirits -  your target was also that of the VAMPIRE' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TALLY','ENG','Tallying Votes...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIE','ENG','A tie. Randomly choosing one...' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DYING-BREATH','ENG','PLAYER: You''re allowed a single line as your dying breath.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'COULD-NOT-ADD','ENG','PLAYER: could not add you. Sorry, please try again.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'MAX-REACHED','ENG','PLAYER: Sorry, maximum players reached.  Please wait for the next game.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PRIORITY-LIST-ERROR','ENG','PLAYER: Could not add you to priority list. Please try again.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALL-LISTS-FULL','ENG','PLAYER: Sorry, both player and priority lists are full. Please wait for the next game.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You are already in this game.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_DEAD','ENG','PLAYER: Your choice is already dead!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DEAD_CANT_VOTE','ENG','PLAYER: You''re dead. You can''t vote!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CHOICE','ENG','PLAYER: Your choice is not playing.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_VOTE_COMMAND','ENG','PLAYER: Please vote in the valid format ''!v <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_KILL_COMMAND','ENG','PLAYER: Please kill in the valid format ''!k <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_SEE_COMMAND','ENG','PLAYER: Please see in the valid format ''!s <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_VOTED','ENG','PLAYER: You already voted.Vote again tomorrow.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CANT_EAT_SELF','ENG','PLAYER: You cannot eat yourself!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VALID_PLAYER','ENG','PLAYER: Please choose  a valid player.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_PLAYING','ENG','PLAYER: You''re not playing.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_KNOW_HUMAN','ENG','PLAYER: You already know you''re human!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYERS_ALIVE','ENG','Alive - PLAYER' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VAMPIRE_SELECTION_LEFT','ENG','PLAYER: The person you selected has left the game.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LEFT_PRIO_LIST','ENG','A player on the priority list left.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play Vampire: !start. Need MINPLAYERS players.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play now: !start to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, !start <entry_amount> ' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_ENDED','ENG','Joining ends…' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_PAID','ENG','Game over! WINNER_STRING WINNINGS_TEXT' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_FREE','ENG','Game over! CONGRATS!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_CANCELED','ENG','Billing error. Game canceled. No charges' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CANT_KILL_OTHER_VAMPIRE','ENG','PLAYER: You cannot kill the other Vampire PLAYR2' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','PLAYER: ERROR_INPUT invalid. Game not started.' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WINNINGS_NON_ZERO','ENG','CURRENCY WINNINGS!!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WINNINGS_ZERO','ENG','by default. But everyone died and noone won the pot. Sorry!' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'VOTING_TIME_ONLY','ENG','PLAYER: Invalid command. Day time is for the lynch vote. Use ''!v <player>''' FROM bot b WHERE displayName = 'VampireBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NIGHT_TIME_ONLY','ENG','PLAYER: Invalid command. Only the Vampires or Slayer can act at night.' FROM bot b WHERE displayName = 'VampireBot';

-- Update Trivia game

DELETE FROM botconfig WHERE botid = ?;
DELETE FROM botmessage WHERE botid = ?;

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','0' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','45' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','5' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'TriviaBot';

-- Trivia
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Trivia' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Trivia' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'TriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'TriviaBot';


-- One

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('One','OneBot','Play the card game - One','one','com.projectgoth.fusion.botservice.bot.migbot.one.One','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,"(uno_blue) (uno_red) (uno_green) (uno_yellow)");

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','0' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','60' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','2' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'OneBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play One - the card game. CMD_START to start.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play One - the card game. CMD_START to start. Cost CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','PLAYER: ''One'' game in progress... ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_PLAYER','ENG','PLAYER:  PLAYR2 either isn''t playing One, or doesn''t exist' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_FREE','ENG','PLAYER started a game of One. CMD_JOIN to join. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! LEADER wins!! CONGRATS!' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','"One" just started. PLAYER,  CMD_DEAL to deal cards.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You are already in this game.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the game.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SHUFFLING_CARDS','ENG','There are no cards left in the pack.  Shuffling..' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CARD_COUNTS','ENG','Counts of Cards: ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PASSED','ENG','PLAYER passed' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'DRAW_THEN_PASS','ENG','PLAYER: You have to draw first then pass' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TOOK_CARD','ENG','PLAYER took a card from the deck.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_RESET','ENG','PLAYER has reset the game. CMD_START to start new One game' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'STARTED_GAME','ENG','PLAYER started a new game.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_SKIPPED','ENG','PLAYER has been skipped.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_2CARDS_SKIPPED','ENG',' takes 2 extra cards and is skipped.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_4CARDS_SKIPPED','ENG',' takes 4 extra cards and is skipped.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ONE_MORE_PLAYER','ENG','PLAYER: You need at least 1 more player to start a game.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WON_GAME','ENG','PLAYER won the game! \\o/' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_POINTS','ENG',' PLAYER won SCORE points! CMD_START to start new One game' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ONE','ENG','PLAYER has *** ONE ***!" w00t!' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REMOVED_AND_NEXT','ENG','PLAYER has been removed from the game. PLAYR2 it''s your turn now.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WON_BY_DEFAULT','ENG','No other players left so PLAYER wins! \\o/  CMD_START to start new One game' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO_WINNER','ENG','All players left the room - no winner. CMD_START to start new One game' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'COLOR_IS','ENG',' and colour is ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_TURN','ENG','PLAYER: it''s your turn <CMD_PLAY to play, CMD_PASS to pass, CMD_DRAW to draw a card>. Top card is ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_COLOR','ENG','PLAYER: Invalid Color Selection' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CARD','ENG','PLAYER: You cannot play that card or you don''t have it.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CHANGES_COLOR','ENG',' and changes colour to ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TURN_IS_BACK_TO','ENG',', turn goes back to ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SKIPPED','ENG',' skipped.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYS','ENG',' plays ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FOLLOW_LAST_CARD','ENG','PLAYER: You have to play a card following on from the last played card, or a wild.' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_YOUR_TURN','ENG','PLAYER: It is not your turn. ' FROM bot b WHERE displayName = 'OneBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'MAX_REACHED','ENG','PLAYER: Sorry, only 4 players in a game.  Please wait for the next game.' FROM bot b WHERE displayName = 'OneBot';


-- Rock, Paper, Scissors
INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Rock Paper Scissors','RPS','3, 2, 1... Show your hand of destruction!','rps','com.projectgoth.fusion.botservice.bot.migbot.rockpaperscissors.RockPaperScissors','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

-- Heads or Tails
INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Heads or Tails','HeadsOrTails','Toss a coin. Guess heads or tails','hot','com.projectgoth.fusion.botservice.bot.migbot.headsortails.HeadsOrTails','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

-- Star Trivia

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Star Trivia','StarTriviaBot','Challenge yourself or play with friends!','strivia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia2','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','5' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'StarTriviaBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Star Trivia' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Star Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Star Trivia' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Star Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Star Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'StarTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'StarTriviaBot';


-- Dice

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Dice','DiceBot','Roll the dice, match or beat the bot!','dice','com.projectgoth.fusion.botservice.bot.migbot.dice.Dice','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountJoinPot','5' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','60' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerRoll','10' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','2' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'DiceBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','TIME''S UP! Tallying rolls... ' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','Dice is on now. Last player standing wins all!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play Dice: CMD_START. Need MINPLAYERS players.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play now: CMD_START to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, CMD_START <entry_amount> ' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_PAID','ENG','Dice started. CMD_JOIN to join. Cost CURRENCY AMOUNT_POT. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_FREE','ENG','Dice started. CMD_JOIN to join. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Game begins! Bot rolls first - match or beat total to stay IN!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_PAID','ENG','Game over! LEADER WINS CURRENCY WINNINGS!! CONGRATS!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_FREE','ENG','Game over! LEADER wins!! CONGRATS!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'BOT_ROLLS_PLAYER','ENG','Bot rolls for PLAYER... ' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'BOT_ROLLED','ENG','ROUND #ROUND_NUMBER: Bot rolled DICE_VALUES. Your TARGET: DICE_TOTAL!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYERS_TURN','ENG','Players: CMD_ROLL to roll. TIMER_ROLL seconds. ' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','PLAYER: ERROR_INPUT invalid. Game not started.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NEXT_ROUND','ENG','Players, get ready for round #ROUND_NUMBER!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_ENDED','ENG','PLAYER: Sorry, a game has already started.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You''re already in the game.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the game.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_CANCELED','ENG','Billing error. Game canceled. No charges' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LEFT','ENG','PLAYER left the game' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_IN_GAME','ENG','PLAYER: you''re not in the game.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ROLLED','ENG','PLAYER: you already rolled.' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEE_OTHERS','ENG','Let''s see what happens next!!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'AUTO_ROLL_MATCH','ENG','Bot rolls - PLAYER: DICE_VALUES IN!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'AUTO_ROLL_HIGHER','ENG','Bot rolls - PLAYER: DICE_VALUES IN!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'AUTO_ROLL_OUT','ENG','Bot rolls - PLAYER: DICE_VALUES OUT! ' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_ROLLS_MATCH','ENG','PLAYER: DICE_VALUES IN!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_ROLLS_HIGHER','ENG','PLAYER: DICE_VALUES IN!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_ROLLS_OUT','ENG','PLAYER: DICE_VALUES OUT!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'IMMUNITY','ENG','PLAYER: DICE_VALUES = immunity for the next round!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SAFE_BY_IMMUNITY','ENG','PLAYER: DICE_VALUES OUT but SAFE by immunity!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALL_LOST_PLAY_AGAIN','ENG','Nobody won, so we''ll try again!' FROM bot b WHERE displayName = 'DiceBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LOST','ENG','PLAYER: sorry you LOST!' FROM bot b WHERE displayName = 'DiceBot';


UPDATE bot set EmoticonKeyList = "(d1) (d2) (d3) (d4) (d5) (d6)" where displayName = 'DiceBot';

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerNewRound','5' FROM bot b WHERE displayName = 'DiceBot';

UPDATE botmessage set messageValue = 'Players, next round starts in TIMER_ROUND seconds!' WHERE messageKey = 'NEXT_ROUND' and botid = 9 and languageCode = 'ENG';

-- LowCard

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('LowCard','LowCardBot','Draw a card. Lowest card is OUT!','lowcard','com.projectgoth.fusion.botservice.bot.migbot.lowcard.LowCard','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountJoinPot','5' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerJoinGame','60' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerDraw','20' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerNewRound','5' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'minPlayers','2' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'LowCardBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','TIME''S UP! Tallying cards... ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','LowCard is on now. Last player standing wins all!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','Play LowCard: CMD_START. Need MINPLAYERS players.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','Play now: CMD_START to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, CMD_START <entry_amount> ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_PAID','ENG','LowCard started. CMD_JOIN to join. Cost CURRENCY AMOUNT_POT. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOIN_FREE','ENG','LowCard started. CMD_JOIN to join. TIMER_JOIN seconds' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Game begins - Lowest card is OUT!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_PAID','ENG','Game over! LEADER WINS CURRENCY WINNINGS!! CONGRATS!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_FREE','ENG','Game over! LEADER wins!! CONGRATS!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'BOT_DRAWS_PLAYER','ENG','Bot draws for PLAYER... ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYERS_TURN','ENG','ROUND #ROUND_NUMBER: Players, CMD_DRAW to DRAW. TIMER_DRAW seconds. ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','PLAYER: ERROR_INPUT invalid. Game not started.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NEXT_ROUND','ENG','All players,  next round in TIMER_ROUND seconds!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_ENDED','ENG','PLAYER: Sorry, a game has already started.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_IN_GAME','ENG','PLAYER: You''re already in the game.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN','ENG','PLAYER joined the game.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'JOIN_NO_MIN','ENG','Joining ends. Not enough players. Need MINPLAYERS.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_CANCELED','ENG','Billing error. Game canceled. No charges' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LEFT','ENG','PLAYER left the game' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NOT_IN_GAME','ENG','PLAYER: you''re not in the game.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_DRAWN','ENG','PLAYER: you already drew.' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SEE_OTHERS','ENG','Let''s see what happens next!!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'AUTO_DRAW','ENG','Bot draws - PLAYER: CARD' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_DRAWS','ENG','PLAYER: CARD' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_TIES','ENG','Tie! PLAYER: CARD ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALL_LOST_PLAY_AGAIN','ENG','Nobody won, so we''ll try again!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LOST','ENG','PLAYER: sorry you LOST!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIEBREAKER_ROUND','ENG','Tied players ONLY draw again. Next round in TIMER_ROUND seconds!' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIED_PLAYERS_LEFT','ENG','Tied players ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALL_PLAYERS_LEFT','ENG','Players are ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ONLY_TIED_PLAYERS','ENG','PLAYER: Only tied players can draw now. Please wait... ' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_LOWCARD','ENG','PLAYER: OUT with the lowest card! CARD' FROM bot b WHERE displayName = 'LowCardBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'PLAYER_TIEBREAK_LOWCARD','ENG','Tie broken! PLAYER: OUT with the lowest card! CARD' FROM bot b WHERE displayName = 'LowCardBot';


INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','StarTriviaQuestions' FROM bot b WHERE displayName = 'StarTriviaBot';

-- Movie Trivia

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Movie Trivia','SportTriviaBot','Movie trivia - challenge yourself or play with friends!','spivia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','5' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','MovieTriviaQuestions' FROM bot b WHERE displayName = 'SportTriviaBot';


INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Movie Trivia' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Sport Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Movie Trivia' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Movie Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Movie Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'SportTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'SportTriviaBot';

-- 9/30/09
UPDATE botconfig set propertyvalue = 10 where propertyname = 'numberOfQuestions' and botid in (8, 11);

-- Movie trivia

UPDATE bot SET game='Movie Trivia', displayname ='MovieTriviaBot', description='Movie trivia - challenge yourself or play with friends!', commandname = 'movia' WHERE id = 12;

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','10' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','MovieTriviaQuestions' FROM bot b WHERE displayName = 'MovieTriviaBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Movie Trivia' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Movie Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Movie Trivia' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Movie Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Movie Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'MovieTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'MovieTriviaBot';

-- Music Trivia
INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Music Trivia','MusicTriviaBot','Music trivia - challenge yourself or play with friends!','musivia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','10' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','MusicTriviaQuestions' FROM bot b WHERE displayName = 'MusicTriviaBot';


INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Music Trivia' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Music Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Music Trivia' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Music Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Music Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'MusicTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'MusicTriviaBot';


-- Geography Trivia

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Geography Trivia','GeogTriviaBot','Geography trivia - challenge yourself or play with friends!','geovia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','10' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','GeographyTriviaQuestions' FROM bot b WHERE displayName = 'GeogTriviaBot';


INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Geography Trivia' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Geography Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Geography Trivia' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Geography Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Geography Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'GeogTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'GeogTriviaBot';


-- Bollywood Trivia

INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Bollywood Trivia','BollyTriviaBot','Bollywood trivia - challenge yourself or play with friends!','bollyvia','com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,0,null);

INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'amountStartGame','5' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerAnswer','20' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'numberOfQuestions','10' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerChargeConfirm','20' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'timerIdle','30' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botconfig` (BotID,PropertyName,PropertyValue) SELECT b.id,'source','BollywoodTriviaQuestions' FROM bot b WHERE displayName = 'BollyTriviaBot';


INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_GAME','ENG','CMD_START to start a new game of Bollywood Trivia' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NTH_QUESTION','ENG','#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'LAST_QUESTION','ENG','Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CORRECT_ANSWER','ENG','PLAYER : POINTS! CORRECT_ANSWER is correct!' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INCORRECT_ANSWER','ENG','PLAYER: That is incorrect. Anyone else?' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'SCORES','ENG','Scoreboard: SCORES' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'TIME_UP','ENG','Time''s up! Answer: CORRECT_ANSWER' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'REPEAT_QUESTION','ENG','Repeating Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_STARTED','ENG','A Bollywood Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_NO_AMOUNT','ENG','CMD_START to start a game of Bollywood Trivia' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Bollywood Trivia. CURRENCY AMOUNT_START' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_NOTE','ENG','Bollywood Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Game over! Scoreboard: SCORES' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER_NO_WINNER','ENG','Game over! There''s no winner. Boo!' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'START_CATEGORIES','ENG','Categories are: CATEGORY_STRING' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_CATEGORY','ENG','PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'CATEGORY','ENG','PLAYER: A game will be started in CATEGORY.' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'WAIT_FOR_ANSWER','ENG','Thanks PLAYER! Please wait for the answer.' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ALREADY_ANSWERED','ENG','PLAYER: you had your chance! Please wait.' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ANSWER_FORMAT','ENG','PLAYER: Type ANSWER_FORMAT to answer.' FROM bot b WHERE displayName = 'BollyTriviaBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'FIRST_CORRECT_ANSWER','ENG','PLAYER got it first! POINTS!' FROM bot b WHERE displayName = 'BollyTriviaBot';


-- Questions
INSERT INTO `fusion`.`bot` (Game,DisplayName,Description,CommandName,ExecutableFileName,LibraryPaths,Type,status,EmoticonKeyList) VALUES ('Taring','TaringBot','Be the fastest to answer the Bot\Õs funny and challenging questions in Bahasa Indonesia!','taring','com.projectgoth.fusion.botservice.bot.migbot.questionbot.QuestionBot','/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar',1,1,null);

INSERT INTO `bot` (`Game`, `DisplayName`, `Description`, `CommandName`, `ExecutableFileName`, `LibraryPaths`, `Type`, `Leaderboards`, `EmoticonKeyList`, `SortOrder`, `GroupID`, `Status`) VALUES ('migCricketDD', 'DDCricketBot', 'Play Cricket in your chat rooms! Get the most runs to win. Try not to get out!', 'ddcricket', 'com.projectgoth.fusion.botservice.bot.migbot.cricket.Cricket', '/usr/fusion/Fusion.jar;/usr/fusion/log4j-1.2.9.jar', 1, 1, '(cr1) (cr2) (cr3) (cr4) (cr6) (crBowled) (crStumped) (crCatch) (crHitWicket) (crLBW) (crRunOut) (crThirdUmpire)', 110, 218, 1);


INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO_GAME_STATE','ENG','Play Cricket. Enter !start to start a game.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTING_STATE','ENG','PLAYER: Cricket Game is starting soon.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOINING_STATE','ENG','Play Cricket. Enter !j to join the game. Cost CURRENCY AMOUNT_POT.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_PLAYING_STATE','ENG','Cricket is on going now. Get ready for the next game.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','Invalid amount. Custom amount has to be CURRENCY 0.05 or more (e.g. !start 5)' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOINING_INPROGRESS','ENG','Game has already started. Enter !j to join the game.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_PLAYING_INPROGRESS','ENG','A game is currently in progress. Please wait for next game.' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROUND_START','ENG','Round #ROUND_NUMBER: Players, Time to hit. !d to bat. TIMER_END seconds' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','Cricket Game over! PLAYER WINS CURRENCY %1! CONGRATS!' FROM bot b WHERE displayName = 'CricketBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'NO_GAME_STATE','ENG','Play DDCricket. Enter !start to start a game.' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTING_STATE','ENG','PLAYER: Game is starting. Munde Dilli Ke!' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOINING_STATE','ENG','Play DDCricket. Enter !j to join the game. Cost CURRENCY %1 .' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_PLAYING_STATE','ENG','DDCricket is on going now. Get ready for the next game.' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'INVALID_AMOUNT','ENG','Invalid amount. Custom amount has to be CURRENCY 0.05 or more (e.g. !start 5)' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_JOINING_INPROGRESS','ENG','Game has already started. Enter !j to join the game.' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_PLAYING_INPROGRESS','ENG','A game is currently in progress. Please wait for next game.' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'ROUND_START','ENG','Round #ROUND_NUMBER: Players, Time to hit. !d to bat. TIMER_END seconds, Go Delhi Daredevils!' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_OVER','ENG','DDCricket Game over! PLAYER WINS CURRENCY %1! CONGRATS!' FROM bot b WHERE displayName = 'DDCricketBot';

INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_STATE','ENG','Cricket Game started. !j to join.Cost CURRENCY AMOUNT_POT . TIMER_JOIN seconds' FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STARTED_STATE','ENG','DDCricket Game started. !j to join.Cost CURRENCY AMOUNT_POT . TIMER_JOIN seconds' FROM bot b WHERE displayName = 'DDCricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_BEGINS','ENG','Game begins - Score the most runs!'  FROM bot b WHERE displayName = 'CricketBot';
INSERT INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_BEGINS','ENG','Game begins - Score the most runs! Munde Dilli Ke' FROM bot b WHERE displayName = 'DDCricketBot';

REPLACE INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of Cricket. Cost: CURRENCY AMOUNT_POT. For custom entry, !start <entry_amount>' FROM bot b WHERE displayName = 'CricketBot';
REPLACE INTO `fusion`.`botmessage` (BotID,MessageKey,LanguageCode,MessageValue) SELECT b.id,'GAME_STATE_DEFAULT_AMOUNT','ENG','CMD_START to start a game of DDCricket. Cost: CURRENCY AMOUNT_POT. For custom entry, !start <entry_amount>' FROM bot b WHERE displayName = 'DDCricketBot';
insert into botconfig
select a.id,b.propertyName,b.propertyValue,b.description from bot a, botconfig b,bot c where a.DisplayName='DDCricketBot' and c.DisplayName='CricketBot' and b.`BotID`=c.id;
