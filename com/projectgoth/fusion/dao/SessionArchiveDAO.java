package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.restapi.data.SSOSessionMetrics;
import com.projectgoth.fusion.sessioncache.SessionArchiveDetail;
import java.util.List;

public interface SessionArchiveDAO {
   void archiveSession(SessionArchiveDetail var1);

   void bulkArchiveSession(List<SessionArchiveDetail> var1);

   void bulkArchiveSSOSessionMetrics(List<SSOSessionMetrics> var1);

   void createSSOSessionMetricsTableIfNotExists();
}
