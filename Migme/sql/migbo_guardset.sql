INSERT INTO guardcapability VALUES (10, 'MIGBO_ACCESS', 1);
INSERT INTO guardcapability VALUES (11, 'MIGBO_CREATE_POST', 1);
INSERT INTO guardset VALUES (4, 'migbo_access');
INSERT INTO guardsetcapability VALUES (3, 10, 4, 1);
INSERT INTO guardsetmember VALUES (NULL, 4, 33, 3);
