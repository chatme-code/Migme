ALTER TABLE groups ADD COLUMN Type tinyint unsigned NOT NULL DEFAULT 0 AFTER id;
ALTER TABLE groups ADD COLUMN AllowNonMembersToJoinRooms BOOLEAN AFTER status;

