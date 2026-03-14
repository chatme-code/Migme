ALTER TABLE `virtualgift`
ADD COLUMN `GroupID` int(11) NULL AFTER `SortOrder`,
ADD COLUMN `GroupVIPOnly` BOOLEAN NULL AFTER `GroupID`,
ADD FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`);

ALTER TABLE `emoticonpack`
ADD COLUMN `GroupID` int(11) NULL AFTER `Price`,
ADD COLUMN `GroupVIPOnly` BOOLEAN NULL AFTER `GroupID`,
ADD FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`);

ALTER TABLE `content`
ADD COLUMN `GroupID` int(11) NULL AFTER `ProviderID`,
ADD COLUMN `GroupVIPOnly` BOOLEAN NULL AFTER `GroupID`,
ADD FOREIGN KEY (`GroupID`) REFERENCES `groups` (`ID`);

/* CONTENT */
/*
INSERT INTO contentcategory VALUES (59, 'IM3 VIP Ringtones', NULL);
INSERT INTO contentcategory VALUES (60, 'IM3 VIP Wallpapers', NULL);
INSERT INTO contentcategory VALUES (61, 'IM3 VIP Games', NULL);
*/
INSERT INTO content (contentcategoryid, contentproviderid, TYPE, NAME, countryid,price, currency, wholesalecost, wholesalecostcurrency,
preview, previewwidth, previewheight, thumbnail, providerid, groupid, groupviponly, STATUS) VALUES
(2, 3, 2, 'Mickey n Friends', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/2230356.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/2230356.gif', 'http://wap.ponselplanet.com/mig33cwp/contentDetail.php?contentId=964', 20, 1, 1),
(2, 3, 2, 'Boo Buttons For Pooh &amp; Friends', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/2232081.jpg', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/2232081.jpg', 'http://wap.ponselplanet.com/mig33cwp/contentDetail.php?contentId=964', 20, 1, 1),
(2, 3, 2, 'Tink: Graceful and Beautiful Fairy', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/2231499.jpg', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/2231499.jpg', 'http://wap.ponselplanet.com/mig33cwp/contentDetail.php?contentId=3781', 20, 1, 1),
(2, 3, 2, 'Princess In Every Girl', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/2230873.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/2230873.gif', 'http://wap.ponselplanet.com/mig33cwp/contentDetail.php?contentId=2466', 20, 1, 1),
(2, 3, 2, 'Chip &amp; Dale Working', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/2231224.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/2231224.gif', 'http://wap.ponselplanet.com/mig33cwp/contentDetail.php?contentId=3200', 20, 1, 1);

INSERT INTO content (contentcategoryid, contentproviderid, TYPE, NAME, countryid,price, currency, wholesalecost, wholesalecostcurrency, preview, previewwidth, previewheight, thumbnail, providerid, groupid, groupviponly, STATUS) VALUES
(4, 3, 4, 'Bejeweled', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/5100043.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/5100043.gif', 'http://wap.ponselplanet.com/mig33/7651', 20, 1, 1),
(4, 3, 4, 'Skate', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/5100042.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/5100042.gif', 'http://wap.ponselplanet.com/mig33/7650', 20, 1, 1),
(4, 3, 4, 'FIFA Street 2', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/5100041.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/5100041.gif', 'http://wap.ponselplanet.com/mig33/7649', 20, 1, 1),
(4, 3, 4, 'Euro 2008', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/5100040.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/5100040.gif', 'http://wap.ponselplanet.com/mig33/7653', 20, 1, 1),
(4, 3, 4, 'The Sims 2', NULL, 0.000, 'IDR', 0, 'IDR', 'http://www.mig33.com/img/mobile_content/elasitas/128x128/5100039.gif', 128, 128, 'http://www.mig33.com/img/mobile_content/elasitas/40x40/5100039.gif', 'http://wap.ponselplanet.com/mig33/7652', 20, 1, 1);
