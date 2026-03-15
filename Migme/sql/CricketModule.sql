CREATE TABLE `groupmodule` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupID` int(11) NOT NULL,
  `Title` varchar(128) NOT NULL,
  `DateCreated` datetime NOT NULL,
  `CreatedBy` varchar(128) NOT NULL,
  `LastModifiedDate` datetime NULL,
  `LastModifiedBy` varchar(128) NULL,
  `Position` int(11) NULL,
  `Type` int(11) NULL,
  `Status` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`),
  FOREIGN KEY (`CreatedBy`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grouppost` (
  `ID` int(11) NOT NULL auto_increment,
  `GroupModuleID` int(11) NOT NULL,
  `Teaser` varchar(1024) NOT NULL,
  `Body` text NULL,
  `DateCreated` datetime NOT NULL,
  `CreatedBy` varchar(128) NOT NULL,
  `LastModifiedDate` datetime NULL,
  `LastModifiedBy` varchar(128) NULL,
  `Status` int(11) NOT NULL,
  PRIMARY KEY  (`ID`),
  FOREIGN KEY (`GroupModuleID`) REFERENCES `groupmodule` (`ID`),
  FOREIGN KEY `CreatedBy` (`CreatedBy`) REFERENCES `user` (`Username`),
  FOREIGN KEY `LastModifiedBy` (`LastModifiedBy`) REFERENCES `user` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



insert into groupmodule (id, groupid, title, datecreated, createdby, lastmodifieddate, lastmodifiedby, position, type, status) values
(1, 7, 'Recent Matches', now(), 'cricketfans', now(), 'cricketfans', 1, 1, 1),
(2, 7, 'Celebrity Chats', now(), 'cricketfans', now(), 'cricketfans', 2, 1, 1),
(3, 7, 'Other Cricket Groups', now(), 'cricketfans', now(), 'cricketfans', 3, 1, 1),
(4, 11, 'Celebrity Chats', now(), 'ddgroup', now(), 'ddgroup', 2, 1, 1);

insert into grouppost(id, groupmoduleid, teaser, body, datecreated, createdby, lastmodifieddate, lastmodifiedby, status) values
(1, 1, '<p><b>Apr 17, 2009 AUS v. SA 5th ODI<br>AUS won by 47 runs</b><br>SA: 256 all out (45.5 ov)<br>AUS: 303/7 (50 ov)</p>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(2, 1, '<p><b>Apr 13, 2009 AUS v. SA 4th ODI<br>SA won by 61 runs</b><br>SA: 317/6 (50 ov)<br>AUS: 256 all out (45.5 ov)</p>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(3, 1, '<p><b>Apr 9, 2009 AUS v. SA 3rd ODI<br>SA won by 25 runs</b><br>SA: 289/6 (50 ov)<br>AUS: 264/7 (50 ov)</p>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(4, 1, '<p><b>Apr 5, 2009 AUS v. SA 2nd ODI<br>SA won by 7 wickets (w 142 balls remaining)</b><br>AUS: 131 all out (40.2 ov)<br>SA 132/3 (26.2 ov)</p>', null, now(), 'cricketfans', now(), 'cricketfans', 1);

insert into grouppost(id, groupmoduleid, teaser, body, datecreated, createdby, lastmodifieddate, lastmodifiedby, status) values
(5, 2, 'McGrath and Vettori Apr 14, 2009', '<b>McGrath and Vettori Chat Highlights Apr 14, 2009</b><br><center><img src="http://www.mig33.com/images/mcgrath1.jpg" width="150" height="101"></center><br><b>glenn_mcgrath</b> -  hello!<br>** martyman gives a Hug to glenn_mcgrath! **<br><b>ddmoderator</b> - manzoor4mm: R u really glen mcgrath? I cnt belv<br><b>glenn_mcgrath</b> - i am in johannesburg<br><b>glenn_mcgrath</b> - hi manzoor<br><b>ddmoderator</b> - sweet_sunny111: Wat r u expecting in this IPL.<br><b>glenn_mcgrath</b> - expecting our team to win<br><b>ddmoderator</b> - peeyush_rokz: Who do you think will the most crucial player in your team<br><b>glenn_mcgrath</b> - daniel vettori<br><b>glenn_mcgrath</b> - would be the most crucial player, he will talk to you in 5 mins<br><b>glenn_mcgrath</b> - he is sitting beside me<br><b>ddmoderator</b> - angry.lion.rajja: Tel me your target wicket<br><b>glenn_mcgrath</b> - rajja, my target wicket is tendulkar<br><center><img src="http://www.mig33.com/images/mcgrath2.jpg" width="150" height="98"></center><br><b>ddmoderator</b> - t_hunde_r: Vettori,plz tell,what is ur team advantage 2 win?<br><b>daniel_vettori</b> - hi, having glen mcgrath, cause he is so old :p<br><b>ddmoderator</b> - t_hunde_r: Whom u scared 2 bowld?<br><b>daniel_vettori</b> - samir, he is a brilliant player, slogger :x<br><b>ddmoderator</b> - sweet_sunny111: Wats ur nick name,tel me<br><b>daniel_vettori</b> - frankie dettori!<br><br><p>Thanks Glenn and Daniel and good luck to the Delhi Daredevils!<br>Become their friend with them on mig33 by adding daniel_vettori and glenn_mcgrath to your friends list.<br>More celebrity chats coming soon on mig33!</p>', now(), 'cricketfans', now(), 'cricketfans', 1);

insert into grouppost(id, groupmoduleid, teaser, body, datecreated, createdby, lastmodifieddate, lastmodifiedby, status) values
(13, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=14">Bangalore Royal Challengers</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(12, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=9">Chennai Super Kings</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(11, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=10">Deccan Chargers</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(10, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=11">Delhi Daredevils</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(9, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=12">Kings XI Punjab</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(8, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=15">Kolkata Knight Riders</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(7, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=13">Mumbai Indians</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1),
(6, 3, '<a href="index.php?c=group&v=midlet&a=home&cid=8">Rajasthan Royals</a>', null, now(), 'cricketfans', now(), 'cricketfans', 1);

insert into grouppost(id, groupmoduleid, teaser, body, datecreated, createdby, lastmodifieddate, lastmodifiedby, status) values
(14, 4, 'McGrath and Vettori Apr 14, 2009', '<b>McGrath and Vettori Chat Highlights Apr 14, 2009</b><br><center><img src="http://www.mig33.com/images/mcgrath1.jpg" width="150" height="101"></center><br><b>glenn_mcgrath</b> -  hello!<br>** martyman gives a Hug to glenn_mcgrath! **<br><b>ddmoderator</b> - manzoor4mm: R u really glen mcgrath? I cnt belv<br><b>glenn_mcgrath</b> - i am in johannesburg<br><b>glenn_mcgrath</b> - hi manzoor<br><b>ddmoderator</b> - sweet_sunny111: Wat r u expecting in this IPL.<br><b>glenn_mcgrath</b> - expecting our team to win<br><b>ddmoderator</b> - peeyush_rokz: Who do you think will the most crucial player in your team<br><b>glenn_mcgrath</b> - daniel vettori<br><b>glenn_mcgrath</b> - would be the most crucial player, he will talk to you in 5 mins<br><b>glenn_mcgrath</b> - he is sitting beside me<br><b>ddmoderator</b> - angry.lion.rajja: Tel me your target wicket<br><b>glenn_mcgrath</b> - rajja, my target wicket is tendulkar<br><center><img src="http://www.mig33.com/images/mcgrath2.jpg" width="150" height="98"></center><br><b>ddmoderator</b> - t_hunde_r: Vettori,plz tell,what is ur team advantage 2 win?<br><b>daniel_vettori</b> - hi, having glen mcgrath, cause he is so old :p<br><b>ddmoderator</b> - t_hunde_r: Whom u scared 2 bowld?<br><b>daniel_vettori</b> - samir, he is a brilliant player, slogger :x<br><b>ddmoderator</b> - sweet_sunny111: Wats ur nick name,tel me<br><b>daniel_vettori</b> - frankie dettori!<br><br><p>Thanks Glenn and Daniel and good luck to the Delhi Daredevils!<br>Become their friend with them on mig33 by adding daniel_vettori and glenn_mcgrath to your friends list.<br>More celebrity chats coming soon on mig33!</p>', now(), 'ddgroup', now(), 'ddgroup', 1);

-- Create the "Recent Matches" module for each IPL group
insert into groupmodule (id, groupid, title, datecreated, createdby, lastmodifieddate, lastmodifiedby, position, type, status) values
(5, 8, 'Recent Matches', now(), 'rrgroup', now(), 'rrgroup', 1, 1, 1),
(6, 9, 'Recent Matches', now(), 'cskgroup', now(), 'cskgroup', 1, 1, 1),
(7, 10, 'Recent Matches', now(), 'dcgroup', now(), 'dcgroup', 1, 1, 1),
(8, 11, 'Recent Matches', now(), 'ddgroup', now(), 'ddgroup', 1, 1, 1),
(9, 12, 'Recent Matches', now(), 'kxipgroup', now(), 'kxipgroup', 1, 1, 1),
(10, 13, 'Recent Matches', now(), 'migroup', now(), 'migroup', 1, 1, 1),
(11, 14, 'Recent Matches', now(), 'rcgroup', now(), 'rcgroup', 1, 1, 1),
(12, 15, 'Recent Matches', now(), 'kkrgroup', now(), 'kkrgroup', 1, 1, 1);

