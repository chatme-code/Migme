DROP TABLE IF EXISTS pendingContact; 

CREATE 
    TABLE pendingcontact 
    ( 
		username varchar(128) NOT NULL default '',
		pendingContact varchar(128) NOT NULL default '',
        PRIMARY KEY USING BTREE (username, pendingContact),
        CONSTRAINT FK_pendingContact_1 FOREIGN KEY (username) REFERENCES user (username),
        CONSTRAINT FK_pendingContact_2 FOREIGN KEY (pendingContact) REFERENCES user (username) 
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;
