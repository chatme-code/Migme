package com.projectgoth.fusion.dao.impl;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.cache.ContactList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.dao.ContactDAO;
import com.projectgoth.fusion.data.ContactData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class ContactDAOJDBC extends MigJdbcDaoSupport implements ContactDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ContactDAOJDBC.class));
   private static MemCachedClient contactListMemcache;

   public Set<ContactData> getContactListForUser(String username) {
      Set<ContactData> contactList = ContactList.getContactList(contactListMemcache, username);
      if (contactList == null) {
         if (this.log.isDebugEnabled()) {
            this.log.debug("no contactlist found in memcached for user [" + username + "]");
         }

         contactList = this.loadContactListFromDB(username);
         if (this.log.isDebugEnabled()) {
            this.log.debug("putting contactlist found in memcached for user [" + username + "]");
         }

         ContactList.setContactList(contactListMemcache, username, contactList);
      } else if (this.log.isDebugEnabled()) {
         this.log.debug("returning cached contactlist for user [" + username + "]");
      }

      return contactList;
   }

   private Set<ContactData> loadContactListFromDB(String username) {
      List<ContactData> contactList = this.getJdbcTemplate().query(this.getExternalizedQuery("ContactDAO.getContactListForUser"), new Object[]{username}, new ContactDAOJDBC.ContactDataRowMapper());
      Set<ContactData> contacts = null;
      if (contactList == null) {
         contacts = ContactList.newContactList();
      } else {
         contacts = ContactList.newContactList(contactList);
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("found " + contacts.size() + "contacts in DB for user [" + username + "]");
      }

      return contacts;
   }

   static {
      contactListMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.contactList);
   }

   private static final class ContactDataRowMapper implements RowMapper {
      private ContactDataRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return new ContactData(rs);
      }

      // $FF: synthetic method
      ContactDataRowMapper(Object x0) {
         this();
      }
   }
}
