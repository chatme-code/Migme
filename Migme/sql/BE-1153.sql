-- Setup clientversion table
CREATE TABLE IF NOT EXISTS clientversion (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, clientType INT, clientVersion INT );
INSERT INTO clientversion (clienttype, clientversion) VALUES (1, 400);
INSERT INTO clientversion (clienttype, clientversion) VALUES (2, 400);
INSERT INTO clientversion (clienttype, clientversion) VALUES (7, 400);
INSERT INTO clientversion (clienttype, clientversion) VALUES (5, 0);
INSERT INTO clientversion (clienttype, clientversion) VALUES (16, 0);
INSERT INTO clientversion (clienttype, clientversion) VALUES (8, 100);
INSERT INTO clientversion (clienttype, clientversion) VALUES (13, 100);
INSERT INTO clientversion (clienttype, clientversion) VALUES (15, 100);
INSERT INTO clientversion (clienttype, clientversion) VALUES (17, 100);

-- Setup guardset tables
INSERT INTO guardset (name) VALUES ('EMOTE_MIN_CLIENT_VERSION');
INSERT INTO guardcapability (id, name, type) VALUES (22, 'EMOTE_MIN_CLIENT_VERSION_DEFAULT', 1);
INSERT INTO guardsetcapability (guardcapabilityid, guardsetid, capabilitytype) 
VALUES (23, (SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), 2);

INSERT INTO guardsetmember (guardsetid, memberid, membertype)
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=1), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype)
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=2), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=7), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=5), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=8), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=13), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=15), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=16), 4);
INSERT INTO guardsetmember (guardsetid, memberid, membertype) 
VALUES ((SELECT id FROM guardset WHERE name='EMOTE_MIN_CLIENT_VERSION'), (SELECT id FROM clientversion WHERE clienttype=17), 4);
