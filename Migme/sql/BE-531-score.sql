DROP TABLE IF EXISTS Score; 

CREATE 
    TABLE Score 
    ( 
    	userid INT UNSIGNED NOT NULL,
    	score INT NOT NULL,
		lastUpdated TIMESTAMP,
        PRIMARY KEY (userid),
        CONSTRAINT FK_score_userid FOREIGN KEY (userid) REFERENCES userid (id)
        
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;
