CREATE TABLE `fusion`.`sweepstakescode`
(
   ID int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   userReferralID int NOT NULL,
   code varchar(10) DEFAULT 'X' NOT NULL,
   dateCreated  timestamp DEFAULT '0000-00-00 00:00:00' NOT NULL,
   username varchar(128) NOT NULL
);

ALTER TABLE sweepstakescode ADD CONSTRAINT FK_SweepsCode_1 FOREIGN KEY (userReferralID) REFERENCES userreferral(ID);
ALTER TABLE sweepstakescode ADD CONSTRAINT FK_SweepsCode_2 FOREIGN KEY (username) REFERENCES user(Username);

CREATE INDEX FK_SweepsCode_1 ON sweepstakescode(userReferralID);
CREATE UNIQUE INDEX Index_code_1 ON sweepstakescode(code);

-- Add some new properties to turn on sweepstakes entry, and associated email messages
INSERT INTO system (PropertyName, PropertyValue) VALUES ('GenerateSweepstakesCode', 1);
INSERT INTO system (PropertyName, PropertyValue) VALUES ('SweepstakesCodeEmailSubject', 'Your \'Live Draw\' Ticket Number');
INSERT INTO system (PropertyName, PropertyValue) VALUES ('SweepstakesCodeEmailBody', 'Thanks for bringing new friends to mig33.\\nYour entry number for winning $2000 / Rp20,000,000 is %1.\\nRefer another friend to get another chance to win, as well as your normal referral bonus.\\nGo to "Live Draw" chat stadium on May 29 at 14:00 GMT to see if you win! \\n\\nFull terms at m.mig33.com/2k');

INSERT INTO system (PropertyName, PropertyValue) VALUES ('ReferralCreditEmailBody2','\\n\\n And, for helping grow the mig33 community to be the largest and best in the world, you will receive by email your entry to our prize draw. Get one additional chance to win $2000 for every friend you introduce!');

