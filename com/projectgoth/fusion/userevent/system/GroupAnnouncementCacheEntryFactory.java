package com.projectgoth.fusion.userevent.system;

import com.projectgoth.fusion.dao.GroupAnnouncementDAO;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class GroupAnnouncementCacheEntryFactory implements CacheEntryFactory {
   private GroupAnnouncementDAO groupAnnouncementDAO;

   public GroupAnnouncementCacheEntryFactory(GroupAnnouncementDAO groupAnnouncementDAO) {
      this.groupAnnouncementDAO = groupAnnouncementDAO;
   }

   public Object createEntry(Object key) throws Exception {
      return this.groupAnnouncementDAO.getGroupAnnouncement((Integer)key);
   }
}
