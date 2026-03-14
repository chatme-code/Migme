DROP TABLE IF EXISTS broadcastlist; 

CREATE 
    TABLE broadcastlist 
    ( 
	username varchar(128) NOT NULL default '',
	broadcastUsername varchar(128) NOT NULL default '',
        PRIMARY KEY USING BTREE (username, broadcastUsername),
        CONSTRAINT FK_broadcastlist_1 FOREIGN KEY (username) REFERENCES user (username),
        CONSTRAINT FK_broadcastlist_2 FOREIGN KEY (broadcastUsername) REFERENCES user (username) 
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;

DELETE FROM system WHERE propertyName = 'BCLPersistSucceedPercentage';
INSERT INTO system VALUES ('BCLPersistSucceedPercentage','1');
