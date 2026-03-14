INSERT INTO clienttext (id, type, description, text) VALUES (41, 2, 'Info text - Virtual gift received', 'You have a %private%gift from %senderusername%! Go to your profile to get it.');

INSERT INTO system VALUES ('VirtualGiftNotificationSMS', '%senderusername% just gave you a %private%mig33 gift! Login to your account to see it now!');

ALTER TABLE `fusion`.`virtualgift` ADD COLUMN `SortOrder` int(11) NULL AFTER `NumSold`;

ALTER TABLE `fusion`.`virtualgift` ADD COLUMN `Location64x64PNG` varchar(128) NULL AFTER `Location16x16PNG`;

UPDATE virtualgift SET Location64x64PNG = REPLACE(Location16x16PNG, '16x16', '64x64');

ALTER TABLE `fusion`.`virtualgiftreceived` ADD COLUMN `Private` int(11) NOT NULL DEFAULT 0 AFTER `Sender`;

ALTER TABLE `fusion`.`virtualgiftreceived` ADD COLUMN `Message` varchar(300) NULL AFTER `Private`;

ALTER TABLE `fusion`.`virtualgiftreceived` ADD COLUMN `PurchaseLocation` int(11) NULL AFTER `DateCreated`;

ALTER TABLE `fusion`.`virtualgiftreceived` ADD COLUMN `Removed` int(11) NOT NULL DEFAULT 0 AFTER `Private`;

INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES (1, 3, 'Send a Gift', 'http://www.mig33.com/midlet/member/store.php?loc=vg&ploc=8&', 300, 399);
INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES (10, 4, 'Send a Gift', 'http://www.mig33.com/midlet/member/store.php?loc=vg&ploc=5&', 300, 399);
INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES (1, 4, 'Send a Gift', 'http://www.mig33.com/midlet/member/store.php?loc=vg&ploc=8&', 402, 499);
INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES (10, 4, 'Send a Gift', 'http://www.mig33.com/midlet/member/store.php?loc=vg&ploc=6&', 402, 499);
INSERT INTO menu (type, position, title, url, minversion, maxversion) VALUES (11, 4, 'Send a Gift', 'http://www.mig33.com/midlet/member/store.php?loc=vg&ploc=5&', 402, 499);

/*
INSERT INTO virtualgift (id, name, hotkey, price, currency, numsold, location12x12gif, location12x12png, location14x14gif, location14x14png, location16x16gif, location16x16png, location64x64png, status) VALUES
(18, 'Be My Valentine', 'vg_cupid_arrow', 0.10, 'USD', 0, '/usr/fusion/emoticons/virtualgifts/cupid_arrow_12x12.gif',  '/usr/fusion/emoticons/virtualgifts/cupid_arrow_12x12.png', '/usr/fusion/emoticons/virtualgifts/cupid_arrow_14x14.gif','/usr/fusion/emoticons/virtualgifts/cupid_arrow_14x14.png','/usr/fusion/emoticons/virtualgifts/cupid_arrow_16x16.gif','/usr/fusion/emoticons/virtualgifts/cupid_arrow_16x16.png', '/usr/fusion/emoticons/virtualgifts/cupid_arrow_64x64.png', 1);

INSERT INTO virtualgift (id, name, hotkey, price, currency, numsold, location12x12gif, location12x12png, location14x14gif, location14x14png, location16x16gif, location16x16png, location64x64png, status) VALUES
(19, 'Happy Valentine''s Day', 'vg_rose', 0.05, 'USD', 0, '/usr/fusion/emoticons/virtualgifts/rose_12x12.gif',  '/usr/fusion/emoticons/virtualgifts/rose_12x12.png', '/usr/fusion/emoticons/virtualgifts/rose_14x14.gif','/usr/fusion/emoticons/virtualgifts/rose_14x14.png','/usr/fusion/emoticons/virtualgifts/rose_16x16.gif','/usr/fusion/emoticons/virtualgifts/rose_16x16.png', '/usr/fusion/emoticons/virtualgifts/rose_64x64.png', 1);
*/

UPDATE virtualgift SET sortorder=1 WHERE id=4;
UPDATE virtualgift SET sortorder=2 WHERE id=7;
UPDATE virtualgift SET sortorder=3 WHERE id=9;
UPDATE virtualgift SET sortorder=4 WHERE id=8;
UPDATE virtualgift SET sortorder=5 WHERE id=2;
UPDATE virtualgift SET sortorder=6 WHERE id=10;
UPDATE virtualgift SET sortorder=7 WHERE id=5;
UPDATE virtualgift SET sortorder=8 WHERE id=6;
UPDATE virtualgift SET sortorder=9 WHERE id=3;
UPDATE virtualgift SET sortorder=10 WHERE id=11;
UPDATE virtualgift SET status=0 where id=1;


