DROP TABLE  IF EXISTS fusion.emotecommand;

CREATE TABLE `fusion`.`emotecommand`
(
   ID int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   ParentID int,
   CommandName varchar(128) NOT NULL,
   CommandStateName varchar(128) NULL,
   Description varchar(128),
   HandlerClassName varchar(128),
   SupportedChatTypes bit(8) DEFAULT 0 NOT NULL, -- ORed bits of supported ChatType, 001 Private, 010 Group, 100 Chatroom
   MessageColour varchar(6),
   MessageText varchar(512),
   EmoticonKeyList varchar(128),
   Price double DEFAULT NULL,
   Currency varchar(6) DEFAULT NULL,
   Status int DEFAULT 0 NOT NULL,
   CONSTRAINT `U_CommandName` UNIQUE (CommandName),
   CONSTRAINT `FK_ParentID` FOREIGN KEY (`ParentID`) REFERENCES `emotecommand` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--ALTER table `emotecommand` ADD COLUMN Price double DEFAULT NULL after EmoticonKeyList, ADD COLUMN Currency varchar(6) DEFAULT NULL after Price;

CREATE TABLE `fusion`.`paidemotesent` (
  `ID` int(11) NOT NULL auto_increment,
  `Username` varchar(128) NOT NULL,
  `DateCreated` datetime NOT NULL,
  `EmoteID` int(11) NOT NULL,
  `PurchaseLocation` smallint DEFAULT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_EmoteID` (`EmoteID`),
  KEY `FK_Username` (`Username`),
  CONSTRAINT `FK_EmoteID` FOREIGN KEY (`EmoteID`) REFERENCES `emotecommand` (`ID`),
  CONSTRAINT `FK_Username` FOREIGN KEY (`Username`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('whackit',null,'Whackit','com.projectgoth.fusion.emote.Whackit',b'111','FF8C00','**(hammer) ;;USER_S;; strength level is: %d%%! %s**','(hammer)',1);

-- throw ball related, share same state
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('throwball','throwball','Throw ball','com.projectgoth.fusion.emote.ThrowBall',b'110','FF8C00','%s throws a ball to %s','',1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('catchball','throwball','Catch ball','com.projectgoth.fusion.emote.ThrowBall',b'110','FF8C00','%s catches the ball!','',1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('stealball','throwball','Steal ball','com.projectgoth.fusion.emote.ThrowBall',b'110','FF8C00','%s steals the ball from %s','',1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('cupid','cupid','Emotion Equation Cupid (EEC)','com.projectgoth.fusion.emote.Cupid',b'111','DD587A','Cupid: %s','',1);
-- love emotes
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('lovematch',null,'single love matching','com.projectgoth.fusion.emote.LoveMatch',b'111','DD587A','**%s (heart-lm) %s: %d%% match!**','(heart-lm)',1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('findmymatch',null,'find love matching','com.projectgoth.fusion.emote.FindMyMatch',b'111','DD587A','**(heart-lm) ;;USER_S;; best match is: %s - %d%%!**','(heart-lm)',1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('flames',null,'flames','com.projectgoth.fusion.emote.Flames',b'111','DD587A','**(flame) FLAMES: %s + %s = %s-%s**','(flame)',1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Status) VALUES ('getmyluck',null,'getmyluck','com.projectgoth.fusion.emote.GetMyLuck',b'111','DD587A','**(luck) ;;USER_S;; Luck for the Day: Love - %d(heart-lm), Money - %d(money), Health - %d(health), Success - %d(success)**','(luck) (heart-lm) (money) (health) (success)',1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('cb','cb','Crystal Ball','com.projectgoth.fusion.emote.CrystalBall',b'110','0B3B39','**(cb) %sCrystalBall%s: %s**','(cb)',0.02,'USD',1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('store',null,'Store','com.projectgoth.fusion.emote.Store',b'111','','','',null,null,1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('help',null,'Help','com.projectgoth.fusion.emote.Help',b'111','','','',null,null,1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('lookout',null,'Lookout','com.projectgoth.fusion.emote.Lookout',b'111','','','',null,null,1);

INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('merchant',null,'Merchant','com.projectgoth.fusion.emote.Merchant',b'111','','','',null,null,1);

-- bot commands
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('bot',null,'Bot','com.projectgoth.fusion.emote.Bot',b'110','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('botstop',null,'BotStop','com.projectgoth.fusion.emote.BotStop',b'110','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('games',null,'Games','com.projectgoth.fusion.emote.Games',b'110','','','',null,null,1);

-- chatroom commands
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('kick',null,'Kick','com.projectgoth.fusion.emote.Kick',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('reserve',null,'Reserve','com.projectgoth.fusion.emote.Reserve',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('mute',null,'Mute','com.projectgoth.fusion.emote.Mute',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('unmute',null,'Unmute','com.projectgoth.fusion.emote.Unmute',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('silence',null,'Silence','com.projectgoth.fusion.emote.Silence',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('adult',null,'Adult','com.projectgoth.fusion.emote.Adult',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('ban',null,'Ban','com.projectgoth.fusion.emote.Ban',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('unban',null,'Unban','com.projectgoth.fusion.emote.Unban',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('lock',null,'Lock','com.projectgoth.fusion.emote.Lock',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('unlock',null,'Unlock','com.projectgoth.fusion.emote.Unlock',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('description',null,'Description','com.projectgoth.fusion.emote.Description',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('invite',null,'Invite','com.projectgoth.fusion.emote.Invite',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('broadcast',null,'Broadcast','com.projectgoth.fusion.emote.Broadcast',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('announce',null,'Announce','com.projectgoth.fusion.emote.Announce',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('gift',null,'Gift','com.projectgoth.fusion.emote.Gift',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('g',null,'Gift','com.projectgoth.fusion.emote.Gift',b'111','','','',null,null,1);


-- paint wars commands
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('paint','pw','PaintWars','com.projectgoth.fusion.emote.PaintWars',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('clean','pw','PaintWars','com.projectgoth.fusion.emote.PaintWars',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('showpaint','pw','PaintWars','com.projectgoth.fusion.emote.PaintWars',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('pw','pw','PaintWars','com.projectgoth.fusion.emote.PaintWars',b'111','','','',null,null,1);

-- friend formation commands
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('follow',null,'Follow','com.projectgoth.fusion.emote.Follow',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('f',null,'Follow','com.projectgoth.fusion.emote.Follow',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('unfollow',null,'Unfollow','com.projectgoth.fusion.emote.Unfollow',b'111','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('uf',null,'Unfollow','com.projectgoth.fusion.emote.Unfollow',b'111','','','',null,null,1);

-- community management emotes
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('mod',null,'Mod','com.projectgoth.fusion.emote.Mod',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('unmod',null,'Unmod','com.projectgoth.fusion.emote.Unmod',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('bump',null,'Bump','com.projectgoth.fusion.emote.Bump',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('bmp',null,'Bump','com.projectgoth.fusion.emote.Bump',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('warn',null,'Warn','com.projectgoth.fusion.emote.Warn',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('w',null,'Warn','com.projectgoth.fusion.emote.Warn',b'100','','','',null,null,1);
INSERT INTO `fusion`.`emotecommand` (CommandName,CommandStateName,Description,HandlerClassName,SupportedChatTypes,MessageColour,MessageText,EmoticonKeyList,Price,Currency,Status) VALUES ('kill',null,'Kill','com.projectgoth.fusion.emote.Kill',b'100','','','',null,null,1);
