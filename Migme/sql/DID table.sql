-- DID table
DROP TABLE didnumber;

CREATE TABLE `fusion`.`DIDNumber` (
  `CountryID` INTEGER UNSIGNED NOT NULL,
  `NUMBER` VARCHAR(128) NOT NULL,
  `Status` INTEGER UNSIGNED NOT NULL,
  CONSTRAINT `FK_DIDNumber_1` FOREIGN KEY `FK_DIDNumber_1` (`CountryID`)
    REFERENCES `country` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
);

INSERT INTO didnumber (countryid, NUMBER, status) VALUES (14, '0280147187', 1);
INSERT INTO didnumber (countryid, NUMBER, status) VALUES (47, '01084181724', 1);  
INSERT INTO didnumber (countryid, NUMBER, status) VALUES (103, '31750967', 1);         
INSERT INTO didnumber (countryid, NUMBER, status) VALUES (136, '0386590057', 1);
INSERT INTO didnumber (countryid, NUMBER, status) VALUES (231, '16505239916', 1);   
	
	