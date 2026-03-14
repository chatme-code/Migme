DROP TABLE IF EXISTS liveIdCredential;

CREATE 
    TABLE liveIdCredential
    ( 
		username varchar(128) NOT NULL default '',
		liveid varchar(128) NOT NULL default '',
		password varchar(128) NOT NULL default '',
		dateRegistered DATETIME NOT NULL default '0000-00-00 00:00:00',
        PRIMARY KEY USING BTREE (username),
        CONSTRAINT FK_liveIdCredential_1 FOREIGN KEY (username) REFERENCES user (username),
        CONSTRAINT UQ_liveIdCredential_1 UNIQUE (liveid)
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;
