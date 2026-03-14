package com.projectgoth.fusion.reputation.cache;

import com.projectgoth.fusion.dao.ReputationDAO;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class ScoreCacheEntryFactory implements CacheEntryFactory {
   private ReputationDAO reputationDAO;

   public ScoreCacheEntryFactory(ReputationDAO groupDAO) {
      this.reputationDAO = groupDAO;
   }

   public Object createEntry(Object key) throws Exception {
      return this.reputationDAO.getUserScore((String)key);
   }
}
