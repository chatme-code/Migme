DROP TABLE IF EXISTS Credential; 

CREATE 
    TABLE Credential 
    ( 
    	userid INT UNSIGNED NOT NULL,
		username VARCHAR(128) NOT NULL,
		password VARCHAR(192) NOT NULL,
		passwordType TINYINT UNSIGNED NOT NULL,
		version TINYINT UNSIGNED NOT NULL DEFAULT '0',
		lastUpdated TIMESTAMP,
		expires DATETIME,
        PRIMARY KEY (userid, passwordType),
        CONSTRAINT FK_credential_userid FOREIGN KEY (userid) REFERENCES userid (id)
        
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;
