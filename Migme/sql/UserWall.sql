CREATE TABLE IF NOT EXISTS UserWallPost 
( 
    	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    	userid INT UNSIGNED NOT NULL,
    	authoruserid INT UNSIGNED NOT NULL,
		datecreated DATETIME NOT NULL,
		body VARCHAR(4000) NOT NULL,
    	numcomments INT NOT NULL DEFAULT '0',
    	numlikes INT NOT NULL DEFAULT '0',
    	numdislikes INT NOT NULL DEFAULT '0',
		type TINYINT NOT NULL,
		status TINYINT NOT NULL DEFAULT '1',
        PRIMARY KEY (id),
        CONSTRAINT FK_userwallpost_userid FOREIGN KEY (userid) REFERENCES userid (id),
        CONSTRAINT FK_userwallpost_authoruserid FOREIGN KEY (authoruserid) REFERENCES userid (id)        
) ENGINE= InnoDB DEFAULT CHARSET= utf8;

CREATE TABLE IF NOT EXISTS UserWallPostComment 
( 
    	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    	userwallpostid INT UNSIGNED NOT NULL,
    	userid INT UNSIGNED NOT NULL,
		datecreated DATETIME NOT NULL,
		comment VARCHAR(4000) NOT NULL,
		status TINYINT NOT NULL DEFAULT '1',
        PRIMARY KEY (id),
        CONSTRAINT FK_userwallpostcomment_userwallpostid FOREIGN KEY (userwallpostid) REFERENCES userwallpost (id),
        CONSTRAINT FK_userwallpostcomment_userid FOREIGN KEY (userid) REFERENCES userid (id)
) ENGINE= InnoDB DEFAULT CHARSET= utf8;

CREATE TABLE IF NOT EXISTS UserWallPostLike 
( 
    	userwallpostid INT UNSIGNED NOT NULL,
    	userid INT UNSIGNED NOT NULL,
		datecreated DATETIME NOT NULL,
		type TINYINT NOT NULL,
        PRIMARY KEY (userwallpostid, userid),
        CONSTRAINT FK_userwallpostlike_userwallpostid FOREIGN KEY (userwallpostid) REFERENCES userwallpost (id),
        CONSTRAINT FK_userwallpostlike_userid FOREIGN KEY (userid) REFERENCES userid (id)
) ENGINE= InnoDB DEFAULT CHARSET= utf8;

CREATE TABLE IF NOT EXISTS UserLike 
( 
    	likeduserid INT UNSIGNED NOT NULL,
    	likinguserid INT UNSIGNED NOT NULL,
		datecreated DATETIME NOT NULL,
		type TINYINT NOT NULL,
        PRIMARY KEY (likeduserid, likinguserid),
        CONSTRAINT FK_userlike_likeduserid FOREIGN KEY (likeduserid) REFERENCES userid (id),
        CONSTRAINT FK_userlike_likinguserid FOREIGN KEY (likinguserid) REFERENCES userid (id)
) ENGINE= INNODB DEFAULT CHARSET= utf8;

CREATE TABLE IF NOT EXISTS UserLikeSummary 
( 
    	userid INT UNSIGNED NOT NULL,
    	numlikes INT NOT NULL DEFAULT '0',
    	numdislikes INT NOT NULL DEFAULT '0',
        PRIMARY KEY (userid),
        CONSTRAINT FK_userlikesummary_userid FOREIGN KEY (userid) REFERENCES userid (id)
) ENGINE= INNODB DEFAULT CHARSET= utf8;

INSERT INTO reputationlevelpermission (ACTION, basemiglevel, STATUS) VALUES ('PostCommentLikeUserWall', 20, 1);
