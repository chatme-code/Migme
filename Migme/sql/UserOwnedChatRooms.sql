/* Dynamic menus */
/*
INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES
(11, 11, 'Room Settings', 'http://www.mig33.com/sites/index.php?c=chatroom&v=midlet&a=setup&',410,410),
(9, 8, 'Room Settings', 'http://www.mig33.com/sites/index.php?c=chatroom&v=midlet&a=setup&',402,405),
(9, 5, 'Room Settings', 'http://www.mig33.com/sites/index.php?c=chatroom&v=midlet&a=setup&',300,399);
*/

/* System properties */
insert into system (propertyname, propertyvalue) values ('ChatRoomOwnershipChangeEmail', 'Test');

/* New language table */
CREATE TABLE `language` (
  `Code` varchar(3) NOT NULL,
  `Name` varchar(128) NOT NULL,
  `Status` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Add ID column to chat room, and appropriate indexes and constraints */
ALTER TABLE `chatroom`
DROP PRIMARY KEY,
ADD COLUMN `ID` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE FIRST,
/*ADD COLUMN `BackgroundPicture` varchar(128) NULL AFTER `MaximumSize`,*/
ADD COLUMN `Language` varchar(3) NULL AFTER `MaximumSize`,
ADD COLUMN `AllowUserKeywords` BOOLEAN NOT NULL DEFAULT 0 AFTER `MaximumSize`,
ADD COLUMN `AllowKicking` BOOLEAN NOT NULL DEFAULT 1 AFTER `MaximumSize`,
ADD COLUMN `UserOwned` BOOLEAN NOT NULL DEFAULT 0 AFTER `MaximumSize`,
ADD COLUMN `NewOwner` varchar(128) NULL AFTER `UserOwned`,
ADD PRIMARY KEY (`ID`),
ADD UNIQUE KEY (`Name`),
/*ADD FOREIGN KEY (`BackgroundPicture`) REFERENCES `file` (`ID`),*/
ADD FOREIGN KEY (`Language`) REFERENCES `language` (`Code`),
ADD FOREIGN KEY (`NewOwner`) REFERENCES `user` (`Username`);

CREATE TABLE `chatroombanneduser` (
  `ChatRoomID` int(11) UNSIGNED NOT NULL,
  `Username` varchar(128) NOT NULL,
  `DateCreated` datetime default NULL,
  PRIMARY KEY (`ChatRoomID`, `Username`),
  FOREIGN KEY (`ChatRoomID`) REFERENCES `chatroom` (`ID`),
  FOREIGN KEY (`Username`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `chatroommoderator` (
  `ChatRoomID` int(11) UNSIGNED NOT NULL,
  `Username` varchar(128) NOT NULL,
  PRIMARY KEY (`ChatRoomID`, `Username`),
  FOREIGN KEY (`ChatRoomID`) REFERENCES `chatroom` (`ID`),
  FOREIGN KEY (`Username`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*ALTER TABLE `chatroom` ADD COLUMN `GroupEventOnly` int(11) NOT NULL DEFAULT 0 AFTER `Name`;*/

CREATE TABLE `chatroomkeyword` (
  `ChatRoomID` int(11) UNSIGNED NOT NULL,
  `KeywordID` int(11) NOT NULL,
  PRIMARY KEY (`ChatRoomID`, `KeywordID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Populate languages with ISO codes */
insert ignore into language (code, name, status) values ('AAR', 'Afar', 0);
insert ignore into language (code, name, status) values ('ABK', 'Abkhazian', 0);
insert ignore into language (code, name, status) values ('AVE', 'Avestan', 0);
insert ignore into language (code, name, status) values ('AFR', 'Afrikaans', 0);
insert ignore into language (code, name, status) values ('AKA', 'Akan', 0);
insert ignore into language (code, name, status) values ('AMH', 'Amharic', 0);
insert ignore into language (code, name, status) values ('ARG', 'Aragonese', 0);
insert ignore into language (code, name, status) values ('ARA', 'Arabic', 0);
insert ignore into language (code, name, status) values ('ASM', 'Assamese', 0);
insert ignore into language (code, name, status) values ('AVA', 'Avaric', 0);
insert ignore into language (code, name, status) values ('AYM', 'Aymara', 0);
insert ignore into language (code, name, status) values ('AZE', 'Azerbaijani', 0);
insert ignore into language (code, name, status) values ('BAK', 'Bashkir', 0);
insert ignore into language (code, name, status) values ('BEL', 'Belarusian', 0);
insert ignore into language (code, name, status) values ('BUL', 'Bulgarian', 0);
insert ignore into language (code, name, status) values ('BIH', 'Bihari', 0);
insert ignore into language (code, name, status) values ('BIS', 'Bislama', 0);
insert ignore into language (code, name, status) values ('BAM', 'Bambara', 0);
insert ignore into language (code, name, status) values ('BEN', 'Bengali', 0);
insert ignore into language (code, name, status) values ('BOD', 'Tibetan', 0);
insert ignore into language (code, name, status) values ('BRE', 'Breton', 0);
insert ignore into language (code, name, status) values ('BOS', 'Bosnian', 0);
insert ignore into language (code, name, status) values ('CAT', 'Catalan', 0);
insert ignore into language (code, name, status) values ('CHE', 'Chechen', 0);
insert ignore into language (code, name, status) values ('CHA', 'Chamorro', 0);
insert ignore into language (code, name, status) values ('COS', 'Corsican', 0);
insert ignore into language (code, name, status) values ('CRE', 'Cree', 0);
insert ignore into language (code, name, status) values ('CES', 'Czech', 0);
insert ignore into language (code, name, status) values ('CHU', 'Church Slavic', 0);
insert ignore into language (code, name, status) values ('CHV', 'Chuvash', 0);
insert ignore into language (code, name, status) values ('CYM', 'Welsh', 0);
insert ignore into language (code, name, status) values ('DAN', 'Danish', 0);
insert ignore into language (code, name, status) values ('DEU', 'German', 0);
insert ignore into language (code, name, status) values ('DIV', 'Divehi', 0);
insert ignore into language (code, name, status) values ('DZO', 'Dzongkha', 0);
insert ignore into language (code, name, status) values ('EWE', 'Ewe', 0);
insert ignore into language (code, name, status) values ('ELL', 'Greek', 0);
insert ignore into language (code, name, status) values ('ENG', 'English', 0);
insert ignore into language (code, name, status) values ('EPO', 'Esperanto', 0);
insert ignore into language (code, name, status) values ('SPA', 'Spanish', 0);
insert ignore into language (code, name, status) values ('EST', 'Estonian', 0);
insert ignore into language (code, name, status) values ('EUS', 'Basque', 0);
insert ignore into language (code, name, status) values ('FAS', 'Persian', 0);
insert ignore into language (code, name, status) values ('FUL', 'Fulah', 0);
insert ignore into language (code, name, status) values ('FIN', 'Finnish', 0);
insert ignore into language (code, name, status) values ('FIJ', 'Fijian', 0);
insert ignore into language (code, name, status) values ('FAO', 'Faroese', 0);
insert ignore into language (code, name, status) values ('FRA', 'French', 0);
insert ignore into language (code, name, status) values ('FRY', 'Frisian', 0);
insert ignore into language (code, name, status) values ('GLE', 'Irish', 0);
insert ignore into language (code, name, status) values ('GLA', 'Scottish Gaelic', 0);
insert ignore into language (code, name, status) values ('GLG', 'Gallegan', 0);
insert ignore into language (code, name, status) values ('GRN', 'Guarani', 0);
insert ignore into language (code, name, status) values ('GUJ', 'Gujarati', 0);
insert ignore into language (code, name, status) values ('GLV', 'Manx', 0);
insert ignore into language (code, name, status) values ('HAU', 'Hausa', 0);
insert ignore into language (code, name, status) values ('HEB', 'Hebrew', 0);
insert ignore into language (code, name, status) values ('HIN', 'Hindi', 0);
insert ignore into language (code, name, status) values ('HMO', 'Hiri Motu', 0);
insert ignore into language (code, name, status) values ('HRV', 'Croatian', 0);
insert ignore into language (code, name, status) values ('HAT', 'Haitian', 0);
insert ignore into language (code, name, status) values ('HUN', 'Hungarian', 0);
insert ignore into language (code, name, status) values ('HYE', 'Armenian', 0);
insert ignore into language (code, name, status) values ('HER', 'Herero', 0);
insert ignore into language (code, name, status) values ('INA', 'Interlingua', 0);
insert ignore into language (code, name, status) values ('IND', 'Indonesian', 0);
insert ignore into language (code, name, status) values ('ILE', 'Interlingue', 0);
insert ignore into language (code, name, status) values ('IBO', 'Igbo', 0);
insert ignore into language (code, name, status) values ('III', 'Sichuan Yi', 0);
insert ignore into language (code, name, status) values ('IPK', 'Inupiaq', 0);
insert ignore into language (code, name, status) values ('IND', 'Indonesian', 0);
insert ignore into language (code, name, status) values ('IDO', 'Ido', 0);
insert ignore into language (code, name, status) values ('ISL', 'Icelandic', 0);
insert ignore into language (code, name, status) values ('ITA', 'Italian', 0);
insert ignore into language (code, name, status) values ('IKU', 'Inuktitut', 0);
insert ignore into language (code, name, status) values ('HEB', 'Hebrew', 0);
insert ignore into language (code, name, status) values ('JPN', 'Japanese', 0);
insert ignore into language (code, name, status) values ('YID', 'Yiddish', 0);
insert ignore into language (code, name, status) values ('JAV', 'Javanese', 0);
insert ignore into language (code, name, status) values ('KAT', 'Georgian', 0);
insert ignore into language (code, name, status) values ('KON', 'Kongo', 0);
insert ignore into language (code, name, status) values ('KIK', 'Kikuyu', 0);
insert ignore into language (code, name, status) values ('KUA', 'Kwanyama', 0);
insert ignore into language (code, name, status) values ('KAZ', 'Kazakh', 0);
insert ignore into language (code, name, status) values ('KAL', 'Greenlandic', 0);
insert ignore into language (code, name, status) values ('KHM', 'Khmer', 0);
insert ignore into language (code, name, status) values ('KAN', 'Kannada', 0);
insert ignore into language (code, name, status) values ('KOR', 'Korean', 0);
insert ignore into language (code, name, status) values ('KAU', 'Kanuri', 0);
insert ignore into language (code, name, status) values ('KAS', 'Kashmiri', 0);
insert ignore into language (code, name, status) values ('KUR', 'Kurdish', 0);
insert ignore into language (code, name, status) values ('KOM', 'Komi', 0);
insert ignore into language (code, name, status) values ('COR', 'Cornish', 0);
insert ignore into language (code, name, status) values ('KIR', 'Kirghiz', 0);
insert ignore into language (code, name, status) values ('LAT', 'Latin', 0);
insert ignore into language (code, name, status) values ('LTZ', 'Luxembourgish', 0);
insert ignore into language (code, name, status) values ('LUG', 'Ganda', 0);
insert ignore into language (code, name, status) values ('LIM', 'Limburgish', 0);
insert ignore into language (code, name, status) values ('LIN', 'Lingala', 0);
insert ignore into language (code, name, status) values ('LAO', 'Lao', 0);
insert ignore into language (code, name, status) values ('LIT', 'Lithuanian', 0);
insert ignore into language (code, name, status) values ('LUB', 'Luba-Katanga', 0);
insert ignore into language (code, name, status) values ('LAV', 'Latvian', 0);
insert ignore into language (code, name, status) values ('MLG', 'Malagasy', 0);
insert ignore into language (code, name, status) values ('MAH', 'Marshallese', 0);
insert ignore into language (code, name, status) values ('MRI', 'Maori', 0);
insert ignore into language (code, name, status) values ('MKD', 'Macedonian', 0);
insert ignore into language (code, name, status) values ('MAL', 'Malayalam', 0);
insert ignore into language (code, name, status) values ('MON', 'Mongolian', 0);
insert ignore into language (code, name, status) values ('MOL', 'Moldavian', 0);
insert ignore into language (code, name, status) values ('MAR', 'Marathi', 0);
insert ignore into language (code, name, status) values ('MSA', 'Malay', 0);
insert ignore into language (code, name, status) values ('MLT', 'Maltese', 0);
insert ignore into language (code, name, status) values ('MYA', 'Burmese', 0);
insert ignore into language (code, name, status) values ('NAU', 'Nauru', 0);
insert ignore into language (code, name, status) values ('NOB', 'Norwegian Bokmål', 0);
insert ignore into language (code, name, status) values ('NDE', 'North Ndebele', 0);
insert ignore into language (code, name, status) values ('NEP', 'Nepali', 0);
insert ignore into language (code, name, status) values ('NDO', 'Ndonga', 0);
insert ignore into language (code, name, status) values ('NLD', 'Dutch', 0);
insert ignore into language (code, name, status) values ('NNO', 'Norwegian Nynorsk', 0);
insert ignore into language (code, name, status) values ('NOR', 'Norwegian', 0);
insert ignore into language (code, name, status) values ('NBL', 'South Ndebele', 0);
insert ignore into language (code, name, status) values ('NAV', 'Navajo', 0);
insert ignore into language (code, name, status) values ('NYA', 'Nyanja', 0);
insert ignore into language (code, name, status) values ('OCI', 'Occitan', 0);
insert ignore into language (code, name, status) values ('OJI', 'Ojibwa', 0);
insert ignore into language (code, name, status) values ('ORM', 'Oromo', 0);
insert ignore into language (code, name, status) values ('ORI', 'Oriya', 0);
insert ignore into language (code, name, status) values ('OSS', 'Ossetian', 0);
insert ignore into language (code, name, status) values ('PAN', 'Panjabi', 0);
insert ignore into language (code, name, status) values ('PLI', 'Pali', 0);
insert ignore into language (code, name, status) values ('POL', 'Polish', 0);
insert ignore into language (code, name, status) values ('PUS', 'Pushto', 0);
insert ignore into language (code, name, status) values ('POR', 'Portuguese', 0);
insert ignore into language (code, name, status) values ('QUE', 'Quechua', 0);
insert ignore into language (code, name, status) values ('ROH', 'Raeto-Romance', 0);
insert ignore into language (code, name, status) values ('RUN', 'Rundi', 0);
insert ignore into language (code, name, status) values ('RON', 'Romanian', 0);
insert ignore into language (code, name, status) values ('RUS', 'Russian', 0);
insert ignore into language (code, name, status) values ('KIN', 'Kinyarwanda', 0);
insert ignore into language (code, name, status) values ('SAN', 'Sanskrit', 0);
insert ignore into language (code, name, status) values ('SRD', 'Sardinian', 0);
insert ignore into language (code, name, status) values ('SND', 'Sindhi', 0);
insert ignore into language (code, name, status) values ('SME', 'Northern Sami', 0);
insert ignore into language (code, name, status) values ('SAG', 'Sango', 0);
insert ignore into language (code, name, status) values ('SIN', 'Sinhalese', 0);
insert ignore into language (code, name, status) values ('SLK', 'Slovak', 0);
insert ignore into language (code, name, status) values ('SLV', 'Slovenian', 0);
insert ignore into language (code, name, status) values ('SMO', 'Samoan', 0);
insert ignore into language (code, name, status) values ('SNA', 'Shona', 0);
insert ignore into language (code, name, status) values ('SOM', 'Somali', 0);
insert ignore into language (code, name, status) values ('SQI', 'Albanian', 0);
insert ignore into language (code, name, status) values ('SRP', 'Serbian', 0);
insert ignore into language (code, name, status) values ('SSW', 'Swati', 0);
insert ignore into language (code, name, status) values ('SOT', 'Southern Sotho', 0);
insert ignore into language (code, name, status) values ('SUN', 'Sundanese', 0);
insert ignore into language (code, name, status) values ('SWE', 'Swedish', 0);
insert ignore into language (code, name, status) values ('SWA', 'Swahili', 0);
insert ignore into language (code, name, status) values ('TAM', 'Tamil', 0);
insert ignore into language (code, name, status) values ('TEL', 'Telugu', 0);
insert ignore into language (code, name, status) values ('TGK', 'Tajik', 0);
insert ignore into language (code, name, status) values ('THA', 'Thai', 0);
insert ignore into language (code, name, status) values ('TIR', 'Tigrinya', 0);
insert ignore into language (code, name, status) values ('TUK', 'Turkmen', 0);
insert ignore into language (code, name, status) values ('TGL', 'Tagalog', 0);
insert ignore into language (code, name, status) values ('TSN', 'Tswana', 0);
insert ignore into language (code, name, status) values ('TON', 'Tonga', 0);
insert ignore into language (code, name, status) values ('TUR', 'Turkish', 0);
insert ignore into language (code, name, status) values ('TSO', 'Tsonga', 0);
insert ignore into language (code, name, status) values ('TAT', 'Tatar', 0);
insert ignore into language (code, name, status) values ('TWI', 'Twi', 0);
insert ignore into language (code, name, status) values ('TAH', 'Tahitian', 0);
insert ignore into language (code, name, status) values ('UIG', 'Uighur', 0);
insert ignore into language (code, name, status) values ('UKR', 'Ukrainian', 0);
insert ignore into language (code, name, status) values ('URD', 'Urdu', 0);
insert ignore into language (code, name, status) values ('UZB', 'Uzbek', 0);
insert ignore into language (code, name, status) values ('VEN', 'Venda', 0);
insert ignore into language (code, name, status) values ('VIE', 'Vietnamese', 0);
insert ignore into language (code, name, status) values ('VOL', 'Volapük', 0);
insert ignore into language (code, name, status) values ('WLN', 'Walloon', 0);
insert ignore into language (code, name, status) values ('WOL', 'Wolof', 0);
insert ignore into language (code, name, status) values ('XHO', 'Xhosa', 0);
insert ignore into language (code, name, status) values ('YID', 'Yiddish', 0);
insert ignore into language (code, name, status) values ('YOR', 'Yoruba', 0);
insert ignore into language (code, name, status) values ('ZHA', 'Zhuang', 0);
insert ignore into language (code, name, status) values ('ZHO', 'Chinese', 0);
insert ignore into language (code, name, status) values ('ZUL', 'Zulu', 0);


update language set status = 1 where name = 'Arabic';
update language set status = 1 where name = 'Bengali';
update language set status = 1 where name = 'German';
update language set status = 1 where name = 'English';
update language set status = 1 where name = 'Spanish';
update language set status = 1 where name = 'French';
update language set status = 1 where name = 'Hebrew';
update language set status = 1 where name = 'Hindi';
update language set status = 1 where name = 'Indonesian';
update language set status = 1 where name = 'Javanese';
update language set status = 1 where name = 'Panjabi';
update language set status = 1 where name = 'Portuguese';
update language set status = 1 where name = 'Russian';
update language set status = 1 where name = 'Swahili';
update language set status = 1 where name = 'Thai';
update language set status = 1 where name = 'Tagalog';
update language set status = 1 where name = 'Vietnamese';
update language set status = 1 where name = 'Chinese';
