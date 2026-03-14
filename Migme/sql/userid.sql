DROP TABLE IF EXISTS UserID; 

CREATE 
    TABLE UserID 
    ( 
    	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
		username VARCHAR(128) NOT NULL,
        PRIMARY KEY (id),
        CONSTRAINT FK_userid_username FOREIGN KEY (username) REFERENCES user (username)
        
    ) 
    ENGINE= InnoDB DEFAULT CHARSET= utf8;

ALTER TABLE UserID AUTO_INCREMENT = 123456789;
    
--CREATE TRIGGER user_userid_insert AFTER INSERT ON User FOR EACH ROW INSERT INTO userid (username) values (NEW.username);

--CREATE TRIGGER user_userid_delete BEFORE DELETE ON User FOR EACH ROW DELETE FROM userid where username = OLD.username;

--INSERT INTO userid (username) SELECT username FROM USER;
