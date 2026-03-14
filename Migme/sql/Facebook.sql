
alter table `fusion`.`messagestats` 
add column `FacebookSent` int(11) DEFAULT '0' NOT NULL after `GTalkReceived`, 
add column `FacebookReceived` int(11) DEFAULT '0' NOT NULL after `FacebookSent`;