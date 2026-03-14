package com.projectgoth.fusion.userevent.system;

import com.projectgoth.fusion.dao.GroupDAO;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class GroupCacheEntryFactory implements CacheEntryFactory {
   private GroupDAO groupDAO;

   public GroupCacheEntryFactory(GroupDAO groupDAO) {
      this.groupDAO = groupDAO;
   }

   public Object createEntry(Object key) throws Exception {
      return this.groupDAO.getGroup((Integer)key);
   }
}
