package com.projectgoth.fusion.authentication.domain;

import com.projectgoth.fusion.authentication.CredentialVersion;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionBusinessException;
import com.projectgoth.fusion.slice.FusionException;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.util.StringUtils;

public class PersistedCredential implements Serializable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PersistedCredential.class));
   private int userID;
   private String username;
   private String password;
   private byte passwordType;
   private CredentialVersion version;
   private Timestamp lastUpdated;
   private Date expires;

   public PersistedCredential(int userid, String username, String password, byte passwordType, CredentialVersion version) {
      this.userID = userid;
      this.username = username;
      this.password = password;
      this.passwordType = passwordType;
      this.version = version;
   }

   public PersistedCredential(int userid, String username, String password, byte passwordType, CredentialVersion version, Timestamp lastUpdated, Date expires) {
      this.userID = userid;
      this.username = username;
      this.password = password;
      this.passwordType = passwordType;
      this.version = version;
      this.lastUpdated = lastUpdated;
      this.expires = expires;
   }

   public int getUserID() {
      return this.userID;
   }

   public String getUsername() {
      return this.username;
   }

   public String getPassword() {
      return this.password;
   }

   public byte getPasswordType() {
      return this.passwordType;
   }

   public CredentialVersion getVersion() {
      return this.version;
   }

   public Timestamp getLastUpdated() {
      return this.lastUpdated;
   }

   public Date getExpires() {
      return this.expires;
   }

   public void secure(Crypter crypter, CredentialVersion version) throws KeyczarException {
      if (version == null || version != CredentialVersion.UNENCRYPTED_MIGRATED && version != CredentialVersion.UNENCRYPTED_UNMIGRATED) {
         this.password = PasswordUtils.encryptPassword(this.password, crypter);
         this.version = version;
      }
   }

   public Credential toCredential() {
      Credential credential = new Credential(this.userID, this.username, this.password, this.passwordType);
      return credential;
   }

   public Credential toUnSecuredCredential(Crypter crypter) throws KeyczarException {
      Credential credential = new Credential(this.userID, this.username, PasswordUtils.decryptPassword(this, crypter), this.passwordType);
      return credential;
   }

   public static PersistedCredential fromCredentialSecured(Crypter crypter, Credential userCredential, CredentialVersion version) throws KeyczarException {
      return new PersistedCredential(userCredential.userID, userCredential.username, PasswordUtils.encryptPassword(userCredential.password, crypter), userCredential.passwordType, version);
   }

   public static List<PersistedCredential> fromUserDataSecured(Crypter crypter, UserData userData, CredentialVersion version) throws KeyczarException, FusionException {
      if (userData == null) {
         return new ArrayList();
      } else {
         List<PersistedCredential> results = new ArrayList(5);
         if (!StringUtils.hasLength(userData.username)) {
            throw new FusionBusinessException("UserData must always have a fusion username", 4);
         } else {
            PersistedCredential fusionCredential = new PersistedCredential(userData.userID, userData.username, PasswordUtils.encryptPassword(userData.password, crypter), PasswordType.FUSION.value(), version);
            results.add(fusionCredential);
            return results;
         }
      }
   }
}
