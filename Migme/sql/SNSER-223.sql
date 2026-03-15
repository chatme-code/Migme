INSERT INTO `emotecommand` (`CommandName`, `CommandStateName`, `Description`, `HandlerClassName`, `SupportedChatTypes`, `MessageColour`, `MessageText`, `EmoticonKeyList`, `Price`, `Currency`, `Status`)
VALUES
	('list', NULL, 'List', 'com.projectgoth.fusion.emote.List', b'100', '', '', '', NULL, NULL, 0),
	('banindex', NULL, 'BanIndex', 'com.projectgoth.fusion.emote.BanIndex', b'100', '', '', '', NULL, NULL, 0),
	('kickindex', NULL, 'KickIndex', 'com.projectgoth.fusion.emote.KickIndex', b'100', '', '', '', NULL, NULL, 0);
