package com.projectgoth.fusion.dao.impl;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.authentication.CredentialVersion;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.authentication.cache.CredentialList;
import com.projectgoth.fusion.authentication.domain.PersistedCredential;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.dao.CredentialDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.keyczar.Crypter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

public class CredentialDAOJDBC extends MigJdbcDaoSupport implements CredentialDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CredentialDAOJDBC.class));
   private static MemCachedClient authenticationServiceMemCache;

   public PersistedCredential getCredential(int userID, PasswordType passwordType) {
      try {
         return (PersistedCredential)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("CredentialDAO.getCredential"), new Object[]{userID, passwordType.value()}, new CredentialDAOJDBC.PersistedCredentialRowMapper());
      } catch (EmptyResultDataAccessException var4) {
         return null;
      }
   }

   public PersistedCredential getCredentialFromNewOrOldTable(int userID, PasswordType passwordType, Crypter crypter) {
      List<PersistedCredential> credentials = CredentialList.getCredentialList(authenticationServiceMemCache, userID);
      if (credentials == null) {
         if (this.log.isDebugEnabled()) {
            this.log.debug("no cached credentials found for user ID " + userID);
         }

         credentials = this.getAllCredentials(userID);
         if (credentials != null && !credentials.isEmpty()) {
            if (passwordType == PasswordType.FUSION && PasswordUtils.filterPersistedCredentialByPasswordType(credentials, PasswordType.FUSION) == null) {
               credentials = this.migrateRemainingCredentials(userID, credentials, crypter);
            }
         } else {
            credentials = this.getAllCredentialsFromOldTable(userID);
         }

         if (credentials == null) {
            CredentialList.setCredentialList(authenticationServiceMemCache, userID, new ArrayList());
         } else {
            CredentialList.setCredentialList(authenticationServiceMemCache, userID, credentials);
         }
      } else {
         if (this.log.isDebugEnabled()) {
            this.log.debug("using cached credentials for user ID " + userID);
         }

         if (!credentials.isEmpty() && passwordType == PasswordType.FUSION && PasswordUtils.filterPersistedCredentialByPasswordType(credentials, PasswordType.FUSION) == null) {
            credentials = this.migrateRemainingCredentials(userID, credentials, crypter);
            CredentialList.setCredentialList(authenticationServiceMemCache, userID, credentials);
         }
      }

      return PasswordUtils.filterPersistedCredentialByPasswordType(credentials, passwordType);
   }

   private List<PersistedCredential> migrateRemainingCredentials(int userID, List<PersistedCredential> existingCredentials, Crypter crypter) {
      List<PersistedCredential> oldTableCredentials = this.getAllCredentialsFromOldTable(userID);
      if (oldTableCredentials != null) {
         if (this.log.isDebugEnabled()) {
            this.log.debug("Migrating remaining credentials for user ID " + userID);
         }

         Iterator i$ = oldTableCredentials.iterator();

         while(i$.hasNext()) {
            PersistedCredential oldCredential = (PersistedCredential)i$.next();
            if (PasswordUtils.filterPersistedCredentialByPasswordType(existingCredentials, PasswordType.fromValue(oldCredential.getPasswordType())) == null) {
               try {
                  oldCredential.secure(crypter, CredentialVersion.KEYCZAR);
                  this.createCredential(oldCredential);
                  existingCredentials.add(oldCredential);
               } catch (Exception var8) {
                  this.log.warn("Unable to migrate the " + PasswordType.fromValue(oldCredential.getPasswordType()) + " for the user " + userID + ": " + var8.getMessage());
               }
            }
         }
      }

      return existingCredentials;
   }

   public Byte[] availableCredentialTypes(int userID) {
      Byte[] results = (Byte[])((Byte[])this.getJdbcTemplate().query(this.getExternalizedQuery("CredentialDAO.availableCredentials"), new Object[]{userID}, new MigJdbcDaoSupport.ByteRowMapper(1)).toArray(new Byte[0]));
      return results != null && results.length != 0 ? results : this.availableCredentialsFromOldTable(userID);
   }

   private Byte[] availableCredentialsFromOldTable(int userID) {
      Map<String, Long> map = this.getJdbcTemplate().queryForMap(this.getExternalizedQuery("CredentialDAO.availableCredentialsFromOldTable"), new Object[]{userID});
      if (map != null && !map.isEmpty()) {
         if (this.log.isDebugEnabled()) {
            Iterator i$ = map.keySet().iterator();

            while(i$.hasNext()) {
               String key = (String)i$.next();
               this.log.debug(key + " -> " + map.get(key));
            }
         }

         List<Byte> returnResult = new ArrayList(map.size());
         if (((Long)map.get("fusion")).equals(1L)) {
            returnResult.add(PasswordType.FUSION.value());
         }

         if (((Long)map.get("aim")).equals(1L)) {
            returnResult.add(PasswordType.AIM_IM.value());
         }

         if (((Long)map.get("msn")).equals(1L)) {
            returnResult.add(PasswordType.MSN_IM.value());
         }

         if (((Long)map.get("gtalk")).equals(1L)) {
            returnResult.add(PasswordType.GTALK_IM.value());
         }

         if (((Long)map.get("yahoo")).equals(1L)) {
            returnResult.add(PasswordType.YAHOO_IM.value());
         }

         return (Byte[])returnResult.toArray(new Byte[0]);
      } else {
         return new Byte[0];
      }
   }

   public List<PersistedCredential> getCredentialsByTypes(int userID, PasswordType[] passwordTypes) {
      List<PersistedCredential> results = this.getAllCredentials(userID);
      return results != null && !results.isEmpty() ? PasswordUtils.filterPersistedCredentialsByPasswordTypes(results, passwordTypes) : this.getOldCredentialsByType(userID, passwordTypes);
   }

   private List<PersistedCredential> getOldCredentialsByType(int userID, PasswordType[] passwordTypes) {
      return PasswordUtils.filterPersistedCredentialsByPasswordTypes(this.getAllCredentialsFromOldTable(userID), passwordTypes);
   }

   public List<PersistedCredential> getAllCredentials(int userID) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("CredentialDAO.getAllCredentials"), new Object[]{userID}, new CredentialDAOJDBC.PersistedCredentialRowMapper());
   }

   public List<PersistedCredential> getAllCredentialsFromMaster(int userID) {
      return this.getMasterTemplate().query(this.getExternalizedQuery("CredentialDAO.getAllCredentials"), new Object[]{userID}, new CredentialDAOJDBC.PersistedCredentialRowMapper());
   }

   public List<PersistedCredential> getAllCredentialsFromOldTable(int userID) {
      return (List)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("CredentialDAO.getAllCredentialsFromOldTable"), new Object[]{userID}, new CredentialDAOJDBC.OldCredentialsRowMapper());
   }

   public int createCredential(final PersistedCredential credential) {
      int rows = this.getMasterTemplate().update(new PreparedStatementCreator() {
         public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(CredentialDAOJDBC.this.getExternalizedQuery("CredentialDAO.createCredential"));
            ps.setInt(1, credential.getUserID());
            ps.setString(2, credential.getUsername());
            ps.setString(3, credential.getPassword());
            ps.setByte(4, credential.getPasswordType());
            ps.setByte(5, credential.getVersion().value());
            return ps;
         }
      });
      this.onCredentialUpdated(credential.getUserID());
      return rows;
   }

   public int[] createCredentials(final List<PersistedCredential> credentials) {
      if (credentials != null && !credentials.isEmpty()) {
         TransactionTemplate transactionTemplate = new TransactionTemplate(this.getMasterTransactionManager());
         return (int[])((int[])transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus transactionStatus) {
               Object result = CredentialDAOJDBC.this.getMasterTemplate().batchUpdate(CredentialDAOJDBC.this.getExternalizedQuery("CredentialDAO.createCredential"), new BatchPreparedStatementSetter() {
                  public void setValues(PreparedStatement ps, int i) throws SQLException {
                     ps.setInt(1, ((PersistedCredential)credentials.get(i)).getUserID());
                     ps.setString(2, ((PersistedCredential)credentials.get(i)).getUsername());
                     ps.setString(3, ((PersistedCredential)credentials.get(i)).getPassword());
                     ps.setByte(4, ((PersistedCredential)credentials.get(i)).getPasswordType());
                     ps.setByte(5, ((PersistedCredential)credentials.get(i)).getVersion().value());
                  }

                  public int getBatchSize() {
                     return credentials.size();
                  }
               });
               CredentialDAOJDBC.this.onCredentialUpdated(((PersistedCredential)credentials.get(0)).getUserID());
               return result;
            }
         }));
      } else {
         return new int[0];
      }
   }

   public int updateCredentialPassword(PersistedCredential credential) {
      int rows = this.getMasterTemplate().update(this.getExternalizedQuery("CredentialDAO.updateCredentialPassword"), new Object[]{credential.getPassword(), credential.getVersion().value(), credential.getUserID(), credential.getPasswordType()});
      this.onCredentialUpdated(credential.getUserID());
      return rows;
   }

   public int updateCredentialUsernameAndPassword(PersistedCredential credential) {
      int rows = this.getMasterTemplate().update(this.getExternalizedQuery("CredentialDAO.updateCredentialUsernameAndPassword"), new Object[]{credential.getUsername(), credential.getPassword(), credential.getVersion().value(), credential.getUserID(), credential.getPasswordType()});
      this.onCredentialUpdated(credential.getUserID());
      return rows;
   }

   public int removeCredential(PersistedCredential credential) {
      int rows = this.getMasterTemplate().update(this.getExternalizedQuery("CredentialDAO.removeCredential"), new Object[]{credential.getUserID(), credential.getPasswordType()});
      this.onCredentialUpdated(credential.getUserID());
      return rows;
   }

   public int userIDForUsername(String username) {
      Integer userID = null;
      if (!StringUtil.isBlank(username)) {
         userID = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.USER_ID, username.toLowerCase());
      }

      if (userID == null) {
         userID = (Integer)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("CredentialDAO.userIDForUsername"), new Object[]{username}, new MigJdbcDaoSupport.IntegerRowMapper(1));
         if (!StringUtil.isBlank(username)) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_ID, username.toLowerCase(), userID);
         }
      }

      return userID;
   }

   public boolean credentialExists(String username, PasswordType passwordType) {
      List<PersistedCredential> credentials = this.getCredentialsByUsernameAndPasswordType(username, passwordType);
      return credentials.size() > 0;
   }

   public List<PersistedCredential> getCredentialsByUsernameAndPasswordType(String username, PasswordType passwordType) {
      try {
         return this.getJdbcTemplate().query(this.getExternalizedQuery("CredentialDAO.getCredentialsByUsernameAndType"), new Object[]{username, passwordType.value()}, new CredentialDAOJDBC.PersistedCredentialRowMapper());
      } catch (EmptyResultDataAccessException var4) {
         return new ArrayList();
      }
   }

   private void onCredentialUpdated(int userID) {
      CredentialList.deleteCredentialList(authenticationServiceMemCache, userID);
   }

   private static PersistedCredential getOldFusionPersistedCredentialFromResultSet(ResultSet rs) throws SQLException {
      return !StringUtils.hasLength(rs.getString("username")) ? null : new PersistedCredential(rs.getInt("userID"), rs.getString("username"), rs.getString("password"), PasswordType.FUSION.value(), CredentialVersion.UNENCRYPTED_UNMIGRATED);
   }

   private static PersistedCredential getOldAIMPersistedCredentialFromResultSet(ResultSet rs) throws SQLException {
      return !StringUtils.hasLength(rs.getString("aimusername")) ? null : new PersistedCredential(rs.getInt("userID"), rs.getString("aimusername"), rs.getString("aimpassword"), PasswordType.AIM_IM.value(), CredentialVersion.UNENCRYPTED_UNMIGRATED);
   }

   private static PersistedCredential getOldMSNPersistedCredentialFromResultSet(ResultSet rs) throws SQLException {
      return !StringUtils.hasLength(rs.getString("msnusername")) ? null : new PersistedCredential(rs.getInt("userID"), rs.getString("msnusername"), rs.getString("msnpassword"), PasswordType.MSN_IM.value(), CredentialVersion.UNENCRYPTED_UNMIGRATED);
   }

   private static PersistedCredential getOldGTalkPersistedCredentialFromResultSet(ResultSet rs) throws SQLException {
      return !StringUtils.hasLength(rs.getString("gtalkusername")) ? null : new PersistedCredential(rs.getInt("userID"), rs.getString("gtalkusername"), rs.getString("gtalkpassword"), PasswordType.GTALK_IM.value(), CredentialVersion.UNENCRYPTED_UNMIGRATED);
   }

   private static PersistedCredential getOldYahooPersistedCredentialFromResultSet(ResultSet rs) throws SQLException {
      return !StringUtils.hasLength(rs.getString("yahoousername")) ? null : new PersistedCredential(rs.getInt("userID"), rs.getString("yahoousername"), rs.getString("yahoopassword"), PasswordType.YAHOO_IM.value(), CredentialVersion.UNENCRYPTED_UNMIGRATED);
   }

   static {
      authenticationServiceMemCache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.authenticationService);
   }

   private static final class OldCredentialsRowMapper implements RowMapper {
      private OldCredentialsRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         List<PersistedCredential> results = new ArrayList();
         PersistedCredential fusionCredential = CredentialDAOJDBC.getOldFusionPersistedCredentialFromResultSet(rs);
         if (fusionCredential == null) {
            return results;
         } else {
            results.add(fusionCredential);
            PersistedCredential aimCredential = CredentialDAOJDBC.getOldAIMPersistedCredentialFromResultSet(rs);
            if (aimCredential != null) {
               results.add(aimCredential);
            }

            PersistedCredential msnCredential = CredentialDAOJDBC.getOldMSNPersistedCredentialFromResultSet(rs);
            if (msnCredential != null) {
               results.add(msnCredential);
            }

            PersistedCredential gtalkCredential = CredentialDAOJDBC.getOldGTalkPersistedCredentialFromResultSet(rs);
            if (gtalkCredential != null) {
               results.add(gtalkCredential);
            }

            PersistedCredential yahooCredential = CredentialDAOJDBC.getOldYahooPersistedCredentialFromResultSet(rs);
            if (yahooCredential != null) {
               results.add(yahooCredential);
            }

            return results;
         }
      }

      // $FF: synthetic method
      OldCredentialsRowMapper(Object x0) {
         this();
      }
   }

   private static final class PersistedCredentialRowMapper implements RowMapper {
      private PersistedCredentialRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         PersistedCredential credential = new PersistedCredential(rs.getInt("userID"), rs.getString("username"), rs.getString("password"), rs.getByte("passwordType"), CredentialVersion.fromValue(rs.getByte("version")), rs.getTimestamp("lastUpdated"), rs.getDate("expires"));
         return credential;
      }

      // $FF: synthetic method
      PersistedCredentialRowMapper(Object x0) {
         this();
      }
   }
}
