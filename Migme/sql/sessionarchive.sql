CREATE 
    TABLE sessionarchive 
( 
        id int(11) AUTO_INCREMENT NOT NULL, 
        username varchar(128) NOT NULL, 
        countryID int(11) NOT NULL,
        startDate datetime NOT NULL,
        endDate datetime NOT NULL,
        authenticated boolean,
       	deviceType int(11),
       	connectionType int(11) NOT NULL,
       	remoteAddress varchar(16),
       	clientVersion smallint, 
        PRIMARY KEY (id) 
    ) 
    ENGINE=MyISAM DEFAULT CHARSET=utf8;
    
    CREATE 
    TABLE sessionarchive 
    ( 
        id int(11) AUTO_INCREMENT NOT NULL, 
        username varchar(128) NOT NULL, 
        countryID int(11) NOT NULL,
        startDate datetime NOT NULL,
        endDate datetime NOT NULL,
        authenticated boolean,
       	deviceType int(11),
       	connectionType int(11) NOT NULL,
       	remoteAddress varchar(16),
       	clientVersion smallint, 
        PRIMARY KEY (id) 
    ) 
    ENGINE=InnoDB DEFAULT CHARSET=utf8 DATA DIRECTORY='/ods' INDEX DIRECTORY='/ods';