DROP TABLE IF EXISTS `grouprss`;
DROP TABLE IF EXISTS `groupphotoalbum`;
DROP TABLE IF EXISTS `groupphoto`;
DROP TABLE IF EXISTS `groupphotolike`;
DROP TABLE IF EXISTS `groupphotocomment`;
DROP TABLE IF EXISTS `groupphotocommentlike`;
DROP TABLE IF EXISTS `grouppoll`;
DROP TABLE IF EXISTS `grouppolloption`;
DROP TABLE IF EXISTS `grouppollvote`;
DROP TABLE IF EXISTS `leaderboardgrouplikesalltime`;
DROP TABLE IF EXISTS `leaderboardgroupphotosalltime`;
DROP TABLE IF EXISTS `leaderboardgrouptopicsalltime`;
DROP TABLE IF EXISTS `groupjoinrequest`;


ALTER TABLE groups DROP COLUMN `Featured`;
ALTER TABLE groups DROP COLUMN `Official`;
ALTER TABLE groups DROP COLUMN `NumPhotos`;
ALTER TABLE groups DROP COLUMN `NumForumPosts`;


ALTER TABLE reputationscoretolevel DROP COLUMN `GroupStorageSize`;


UPDATE reputationscoretolevel SET groupsize = 150 WHERE level >= 24 AND level <= 28;
UPDATE reputationscoretolevel SET groupsize = 250 WHERE level >= 29 AND level <= 32;
UPDATE reputationscoretolevel SET groupsize = 500 WHERE level >= 33 AND level <= 36;
UPDATE reputationscoretolevel SET groupsize = 600 WHERE level >= 37 AND level <= 40;
UPDATE reputationscoretolevel SET groupsize = 750 WHERE level >= 41 AND level <= 45;
UPDATE reputationscoretolevel SET groupsize = 1000 WHERE level >= 46 AND level <= 49;
UPDATE reputationscoretolevel SET groupsize = 1500 WHERE level = 50;