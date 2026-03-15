
alter table `fusion`.`handsets` add column `ApplicationIconSize` int(11) NOT NULL after `ScreenHeight`;

update handsets set applicationiconsize = 24;
update handsets set applicationiconsize = 48 where vendor = 'nokia' and phonemodel in ('n73', '6300', '3110c', '6120_classic', '5300', '5310', '6230i', '6233', 'n95');
