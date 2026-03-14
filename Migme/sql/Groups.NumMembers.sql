ALTER TABLE groups ADD COLUMN NumMembers INT(11) NOT NULL DEFAULT 0 AFTER SortOrder;

UPDATE groups, (SELECT groupid, COUNT(*) num FROM groupmember WHERE STATUS=1 GROUP BY groupid) n SET groups.nummembers=n.num WHERE groups.id=n.groupid;
