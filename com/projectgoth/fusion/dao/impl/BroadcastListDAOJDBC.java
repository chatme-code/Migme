package com.projectgoth.fusion.dao.impl;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.cache.BroadcastList;
import com.projectgoth.fusion.cache.ContactList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.dao.BroadcastListDAO;
import com.projectgoth.fusion.dao.ContactDAO;
import com.projectgoth.fusion.data.ContactData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

public class BroadcastListDAOJDBC extends MigJdbcDaoSupport implements BroadcastListDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BroadcastListDAOJDBC.class));
   private static MemCachedClient broadcastListMemcache;
   private ContactDAO contactDAO;

   public Set<String> getBroadcastListForUser(String username) {
      label102: {
         Set var3;
         try {
            if (!MemCachedUtils.getLock(broadcastListMemcache, "BLDL", username, 15000)) {
               this.log.error("Failed to get a lock to update user [" + username + "]'s BCL in 10 seconds");
               break label102;
            }

            Set<String> broadcastList = BroadcastList.getBroadcastList(broadcastListMemcache, username);
            if (broadcastList != null) {
               this.log.debug("found a broadcastlist in memcached");
               var3 = broadcastList;
               return var3;
            }

            broadcastList = this.loadBroadcastListFromDB(username);
            if (broadcastList == null || broadcastList.isEmpty()) {
               broadcastList = this.generateBCL(username);
               BroadcastList.setBroadcastList(broadcastListMemcache, username, broadcastList);
               var3 = broadcastList;
               return var3;
            }

            this.log.debug(username + " already has a BCL, not generating, using the existing one from DB");
            BroadcastList.setBroadcastList(broadcastListMemcache, username, broadcastList);
            var3 = broadcastList;
         } catch (Exception var8) {
            this.log.error("failed to load the BCL, rolling back", var8);
            break label102;
         } finally {
            MemCachedUtils.releaseLock(broadcastListMemcache, "BLDL", username);
         }

         return var3;
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("broadcast list for user [" + username + "] not persisted as it's empty");
      }

      return Collections.emptySet();
   }

   public Set<String> getBroadcastListForGroup(int groupId) {
      Set<String> broadcastList = this.loadGroupBroadcastListFromDB(groupId);
      return broadcastList != null && !broadcastList.isEmpty() ? broadcastList : Collections.emptySet();
   }

   private Set<String> loadBroadcastListFromDB(String username) {
      List<String> broadcastList = this.getJdbcTemplate().query(this.getExternalizedQuery("BroadcastDAO.getBroadcastListForUser"), new Object[]{username}, new BroadcastListDAOJDBC.BroadcastListRowMapper());
      Set<String> broadcastListSet = BroadcastList.newBroadcastList(broadcastList);
      return broadcastListSet;
   }

   private Set<String> loadGroupBroadcastListFromDB(Integer groupId) {
      List<String> broadcastList = this.getJdbcTemplate().query(this.getExternalizedQuery("BroadcastDAO.getBroadcastListForGroup"), new Object[]{groupId}, new BroadcastListDAOJDBC.BroadcastListRowMapper());
      Set<String> broadcastListSet = BroadcastList.newBroadcastList(broadcastList);
      return broadcastListSet;
   }

   private Set<String> generateBCL(String username) throws SQLException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("generating BCL for user [" + username + "]");
      }

      Set<ContactData> contacts = this.contactDAO.getContactListForUser(username);
      if (contacts != null && !contacts.isEmpty()) {
         Set<String> broadcastList = BroadcastList.newBroadcastList(ContactList.fusionContactUsernames(contacts));
         List<String> contactsWhoHaveMe = this.getJdbcTemplate().query("select username from contact where fusionUsername = ?", new Object[]{username}, new BroadcastListDAOJDBC.GenericStringMapper());
         broadcastList.retainAll(contactsWhoHaveMe);
         List<String> contactsUserBlocked = this.getJdbcTemplate().query("select blockusername from blocklist where username = ?", new Object[]{username}, new BroadcastListDAOJDBC.GenericStringMapper());
         broadcastList.removeAll(contactsUserBlocked);
         List<String> contactsBlockedUser = this.getJdbcTemplate().query("select username from blocklist where blockusername = ?", new Object[]{username}, new BroadcastListDAOJDBC.GenericStringMapper());
         broadcastList.removeAll(contactsBlockedUser);
         if (this.log.isDebugEnabled()) {
            this.log.debug("generated " + broadcastList.size() + " BCL entries for user [" + username + "]");
         }

         return broadcastList;
      } else {
         return BroadcastList.newBroadcastList();
      }
   }

   @Required
   public void setContactDAO(ContactDAO contactDAO) {
      this.contactDAO = contactDAO;
   }

   static {
      broadcastListMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }

   private static final class GenericStringMapper implements RowMapper {
      private GenericStringMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getString(1);
      }

      // $FF: synthetic method
      GenericStringMapper(Object x0) {
         this();
      }
   }

   private static final class BroadcastListRowMapper implements RowMapper {
      private BroadcastListRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getString(1);
      }

      // $FF: synthetic method
      BroadcastListRowMapper(Object x0) {
         this();
      }
   }
}
