ALTER TABLE sessionhistorysummary ADD COLUMN uniqueAuth90 int(10) NOT NULL AFTER uniqueNonAuth30;
ALTER TABLE sessionhistorysummary ADD COLUMN uniqueNonAuth90 int(10) NOT NULL AFTER uniqueAuth90;
