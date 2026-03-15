create database stats;

create table registrystats (
	id INT AUTO_INCREMENT PRIMARY KEY,
	statsdatetime TIMESTAMP(14),
	hostname VARCHAR(20) NOT NULL,
	numuserobjectrefs INT,
	maxuserobjectrefs INT,
	objectcaches VARCHAR(200),
	otherregistries VARCHAR(200),
	requestspersecond DOUBLE,
	maxrequestspersecond DOUBLE,
	jvmtotalmemory INT,
	jvmfreememory INT,
	uptime INT,
	online INT NOT NULL);
	
create table objectcachestats (
	id INT AUTO_INCREMENT PRIMARY KEY,
	statsdatetime TIMESTAMP(14),
	hostname VARCHAR(20) NOT NULL,
	numuserobjects INT,
	maxuserobjects INT,
	requestspersecond DOUBLE,
	maxrequestspersecond DOUBLE,
	jvmtotalmemory INT,
	jvmfreememory INT,
	uptime INT,
	online INT NOT NULL);
