DROP TABLE  fusion.bot;

CREATE TABLE `fusion`.`bot`
(
   ID int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   DisplayName varchar(20) NOT NULL,
   DefaultNickPrefix varchar(15) NOT NULL,
   Description varchar(128),
   CommandName varchar(128),
   FileFolder varchar(128) NOT NULL,
   ExecutableFileName varchar(128),
   ExecutableFilePath varchar(128),
   ConfigFileName varchar(128),
   ConfigFilePath varchar(128),
   LibraryPaths varchar(400),
   Type int DEFAULT 1 NOT NULL,
   Creator varchar(128),
   Loaded bit DEFAULT 0 NOT NULL,
   Enabled bit DEFAULT 0 NOT NULL,
   Leaderboards bit DEFAULT 0 NOT NULL,
   ProgramLanguage varchar(10),
   DateCreated timestamp,
   DateLastAccessed timestamp,
   SpecialCommandList varchar(128)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE bot ADD INDEX (ID);
ALTER TABLE bot ADD INDEX (DisplayName);
ALTER TABLE bot ADD INDEX (DateLastAccessed);


-- Stores a row for each bot in a chat room 
DROP TABLE fusion.chatroombot;
CREATE TABLE `fusion`.`chatroombot`
(
   ID int PRIMARY KEY NOT NULL,
   UserName varchar(128),
   Nick varchar(128),
   ChannelName varchar(128),   
   BotID int NOT NULL,   
   Status int DEFAULT 1 NOT NULL,
   DateCreated timestamp,
   DateLastAccessed timestamp,
   Enabled bit DEFAULT 0 NOT NULL
);

ALTER TABLE chatroombot ADD INDEX (ChannelName);
ALTER TABLE chatroombot ADD INDEX (BotID);

ALTER TABLE chatroombot ADD CONSTRAINT FK_chatroombot_room FOREIGN KEY(ChatRoomName) REFERENCES chatroom(name);
ALTER TABLE chatroombot ADD CONSTRAINT FK_chatroombot_bot FOREIGN KEY(BotID) REFERENCES bot(ID);


INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,SpecialCommandList) 
			VALUES ('WerewolfBot','WerewolfBot','Play the Werewolf game','wolf','werewolf','com.projectgoth.fusion.botservice.bot.irc.plugin.werewolf.Werewolf',
			'usr/fusion/Fusion.jar',null, null,'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/log4j-1.2.9.jar',1,
			'LlamaBoy and DarkShine',0,1,0,'Java',now(),now(),'!start,!quit');
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('ChatterBot','ChatterBot','ChatterBot interacts in the room','chatter','jmegahalai','com.projectgoth.fusion.botservice.bot.irc.plugin.jmegahalai.JMegaHalBot',
			'usr/fusion/Fusion.jar',null, null,
			'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/bots/JMegaHal.jar;usr/fusion/log4j-1.2.9.jar',1,
			'LlamaBoy and DarkShine',0,1,0,'Java',now(),now(), null);
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('PuzzleBot','PuzzleBot','30 second math puzzle','puzzle','countdownpuzzle','com.projectgoth.fusion.botservice.bot.irc.plugin.countdownpuzzle.CountdownPuzzle',
			'usr/fusion/Fusion.jar',null, null,
			'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/bots/jep-2.4.1.jar;usr/fusion/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);			
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('UnoBot','UnoBot','Play UNO','uno','uno','com.projectgoth.fusion.botservice.bot.irc.plugin.uno.Uno',
			'usr/fusion/Fusion.jar','unoconfig.xml', null,
			'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/bots/jdom.jar;usr/fusion/bots/saxpath.jar;usr/fusion/bots/jaxen-jdom.jar;usr/fusion/bots/jaxen-core.jar;usr/fusion/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('RussianRoulette','RussianRoulette','Play Russian roulette. Are you lucky?','roul','roulette','com.projectgoth.fusion.botservice.bot.irc.plugin.roulette.RussianRoulette',
			'usr/fusion/Fusion.jar',null, null,
			'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/bots/jdom.jar;usr/fusion/bots/saxpath.jar;usr/fusion/bots/jaxen-jdom.jar;usr/fusion/bots/jaxen-core.jar;usr/fusion/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);	
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('TriviaBot','TriviaBot','Trivia Fun!','trivia','trivia','com.projectgoth.fusion.botservice.bot.irc.plugin.trivia.TriviaBot',
			'usr/fusion/Fusion.jar','trivia.txt', null,
			'usr/fusion/bots/pircbot-1.4.6.jar;usr/fusion/log4j-1.2.9.jar',1,
			'IRC Hacks',0,1,0,'Java',now(),now(), null);
			
			
			

-- Windows SQL
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,SpecialCommandList) 
			VALUES ('WerewolfBot','WerewolfBot','Play the Werewolf game','wolf','werewolf','com.projectgoth.fusion.botservice.bot.irc.plugin.werewolf.Werewolf',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar',null, null,
			 'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/bots/JMegaHal.jar;C:/dev/common/bots/jep-2.4.1.jar;C:/dev/common/bots/log4j-1.2.9.jar',1,
			'LlamaBoy and DarkShine',0,1,0,'Java',now(),now(),'!start,!quit');
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('ChatterBot','ChatterBot','Interacts in the room','chatter','jmegahalai','com.projectgoth.fusion.botservice.bot.irc.plugin.jmegahalai.JMegaHalBot',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar',null, null,
			'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/bots/JMegaHal.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('PuzzleBot','PuzzleBot','30 second math puzzle','puzzle','countdownpuzzle','com.projectgoth.fusion.botservice.bot.irc.plugin.countdownpuzzle.CountdownPuzzle',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar',null, null,
			'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/bots/jep-2.4.1.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('UnoBot','UnoBot','Play UNO','uno','uno','com.projectgoth.fusion.botservice.bot.irc.plugin.uno.Uno',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar','unoconfig.xml', null,
			'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/bots/jdom.jar;C:/dev/common/bots/saxpath.jar;C:/dev/common/bots/jaxen-jdom.jar;C:/dev/common/bots/jaxen-core.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar;C:/dev/common/',1,
			'PircBot',0,1,0,'Java',now(),now(), null);	
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('RussianRoulette','RussianRoulette','Play Russian roulette. Are you lucky?','roul','roulette','com.projectgoth.fusion.botservice.bot.irc.plugin.roulette.RussianRoulette',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar',null, null,
			'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/bots/jdom.jar;C:/dev/common/bots/saxpath.jar;C:/dev/common/bots/jaxen-jdom.jar;C:/dev/common/bots/jaxen-core.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,
			'PircBot',0,1,0,'Java',now(),now(), null);	
			
INSERT INTO `fusion`.`bot` (DisplayName,DefaultNickPrefix,Description,CommandName,FileFolder,ExecutableFileName,ExecutableFilePath,
			ConfigFileName,ConfigFilePath,LibraryPaths,Type,Creator,Loaded,Enabled,Leaderboards,ProgramLanguage,DateCreated,DateLastAccessed,
			SpecialCommandList) 
			VALUES ('TriviaBot','TriviaBot','Trivia Fun!','trivia','trivia','com.projectgoth.fusion.botservice.bot.irc.plugin.trivia.TriviaBot',
			'C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar','trivia.txt', null,
			'C:/dev/common/bots/pircbot-1.4.6.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar',1,
			'IRC Hacks',0,1,0,'Java',now(),now(), null);
						
