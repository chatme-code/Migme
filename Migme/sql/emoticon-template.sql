-- %(alias)s
select * from emoticonhotkey where hotkey='%(hotkey)s';
select * from emoticonpack ep, emoticonhotkey eh, emoticon ei where ep.id=ei.emoticonpackid and eh.emoticonid=ei.id and eh.hotkey='%(hotkey)s';

insert into emoticon (EmoticonPackID, Type, Alias, Width, Height, Location, LocationPNG) values ((select id from emoticonpack where name='%(pack)s'), 1, '%(alias)s', 12, 12, '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_12x12.gif', '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_12x12.png');
insert into emoticon (EmoticonPackID, Type, Alias, Width, Height, Location, LocationPNG) values ((select id from emoticonpack where name='%(pack)s'), 1, '%(alias)s', 14, 14, '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_14x14.gif', '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_14x14.png');
insert into emoticon (EmoticonPackID, Type, Alias, Width, Height, Location, LocationPNG) values ((select id from emoticonpack where name='%(pack)s'), 1, '%(alias)s', 16, 16, '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_16x16.gif', '/usr/fusion/emoticons/%(folder)s/%(fileprefix)s_16x16.png');

insert into emoticonhotkey (emoticonid, type, hotkey) select id, 1,'%(hotkey)s' from emoticon where alias='%(alias)s';

select * from emoticonhotkey eh, emoticon ei where emoticonid=ei.id and ei.emoticonpackid=(select id from emoticonpack where name='%(pack)s' and Alias='%(alias)s');
-- delete from emoticonhotkey where emoticonid in (select id from emoticon where emoticonpackid=(select id from emoticonpack where name='%(pack)s') and Alias='%(alias)s');
-- delete from emoticon where emoticonpackid=(select id from emoticonpack where name='%(pack)s') and Alias='%(alias)s';

