package com.projectgoth.fusion.authentication;

import Ice.Current;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.authentication.cache.CredentialList;
import com.projectgoth.fusion.authentication.domain.PersistedCredential;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.common.UsernameValidationException;
import com.projectgoth.fusion.dao.CredentialDAO;
import com.projectgoth.fusion.data.ValidateCredentialResult;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionBusinessException;
import com.projectgoth.fusion.slice.FusionCredential;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.FusionServerException;
import com.projectgoth.fusion.slice._AuthenticationServiceDisp;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.util.StringUtils;

public class AuthenticationServiceI extends _AuthenticationServiceDisp implements InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AuthenticationServiceI.class));
   private static final Logger authenticationLog = Logger.getLogger("AuthenticationLog");
   private static MemCachedClient commonMemcache;
   private static MemCachedClient authenticationServiceMemCache;
   public static final String MIGRATE_USER_DISTRIBUTED_LOCK_NAMESPACE = "MU_NS";
   private static final String CRYPTER_KEY_LOCATION;
   private CredentialDAO credentialDAO;
   private Crypter crypter;
   private String surgeMailPassword;
   private int suspectIPRatio = 20000;
   private int bruteForceIPRatio = 50000;
   private RequestAndRateLongCounter successfulAuthentications = new RequestAndRateLongCounter(3);
   private RequestAndRateLongCounter failedAuthentications = new RequestAndRateLongCounter(3);
   private ConcurrentMap<String, AtomicInteger> failedBreakdown = new ConcurrentHashMap();
   private ConcurrentMap<String, AtomicInteger> succeededBreakdown = new ConcurrentHashMap();

   public void afterPropertiesSet() throws Exception {
      this.crypter = new Crypter(CRYPTER_KEY_LOCATION);
   }

   void shutdown() {
   }

   public void setCredentialDAO(CredentialDAO credentialDAO) {
      this.credentialDAO = credentialDAO;
   }

   public void setSurgeMailPassword(String surgeMailPassword) {
      this.surgeMailPassword = surgeMailPassword;
   }

   public RequestAndRateLongCounter getSuccessfulAuthentications() {
      return this.successfulAuthentications;
   }

   public RequestAndRateLongCounter getFailedAuthentications() {
      return this.failedAuthentications;
   }

   public void setSuspectIPRatio(int suspectIPRatio) {
      this.suspectIPRatio = suspectIPRatio;
   }

   public void setBruteForceIPRatio(int bruteForceIPRatio) {
      this.bruteForceIPRatio = bruteForceIPRatio;
   }

   public void setMinimumAuthenticationsPerIP(int minimumAuthenticationsPerIP) {
   }

   private void logAuthenticationResult(AuthenticationServiceResponseCodeEnum response, Credential credential, String clientIP) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.LOG_SUCCESSFUL_LOGINS) && response != AuthenticationServiceResponseCodeEnum.Failed) {
         if (authenticationLog.isDebugEnabled()) {
            authenticationLog.debug(response + " for user id [" + (credential == null ? null : credential.userID) + "] and name [" + (credential == null ? null : credential.username) + "] and IP [" + clientIP + "]");
         }
      } else {
         authenticationLog.info(response + " for user id [" + (credential == null ? null : credential.userID) + "] and name [" + (credential == null ? null : credential.username) + "] and IP [" + clientIP + "]");
      }

      if (response == AuthenticationServiceResponseCodeEnum.Success) {
         this.successfulAuthentications.add();
      } else {
         this.failedAuthentications.add();
      }

   }

   private int failedIPRatio(String ip) {
      AtomicInteger fails = (AtomicInteger)this.failedBreakdown.get(ip);
      if (fails == null) {
         return 0;
      } else {
         AtomicInteger succeeds = (AtomicInteger)this.succeededBreakdown.get(ip);
         if (succeeds != null && succeeds.intValue() != 0) {
            return fails.intValue() / succeeds.intValue();
         } else {
            return fails.intValue() > 1000 ? fails.intValue() : 0;
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("authenticating user [" + userCredential.username + "] with type [" + PasswordType.fromValue(userCredential.passwordType) + "]");
      }

      long start = System.currentTimeMillis();
      AuthenticationServiceResponseCodeEnum authenticationResponse = AuthenticationServiceResponseCodeEnum.Failed;

      try {
         if (userCredential.passwordType == PasswordType.FUSION.value()) {
            authenticationResponse = this.handleFusionCredentialAuthentication((FusionCredential)userCredential, clientIP);
         } else if (userCredential.passwordType == PasswordType.MAIL.value()) {
            authenticationResponse = this.handleMailCredentialAuthentication(userCredential);
         }
      } finally {
         long end = System.currentTimeMillis();
         if (log.isDebugEnabled()) {
            log.debug("authenticating user [" + userCredential.username + "] took [" + (end - start) + "] ms");
         }

      }

      this.logAuthenticationResult(authenticationResponse, userCredential, clientIP);
      if (authenticationResponse == AuthenticationServiceResponseCodeEnum.Failed || authenticationResponse == AuthenticationServiceResponseCodeEnum.UnknownUsername) {
         this.increaseAuthFailureScoreForIP(userCredential, clientIP);
      }

      return authenticationResponse;
   }

   private void increaseAuthFailureScoreForIP(Credential userCredential, String clientIP) {
      try {
         DecayingFailedAuthsByIPScore.incrementScore(clientIP, 1.0D);
      } catch (Exception var4) {
         log.error("Failed to update failed auths score for [IP:" + clientIP + "] [USER:" + userCredential.username + "], e=" + var4, var4);
      }

   }

   private boolean isCredentialExpired(PersistedCredential persistedCredential) {
      if (persistedCredential != null && persistedCredential.getExpires() != null) {
         return persistedCredential.getExpires().getTime() < System.currentTimeMillis();
      } else {
         return false;
      }
   }

   private static int getSha1HashCode(String saltedPassword) throws FusionServerException {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] result = md.digest(saltedPassword.getBytes("UTF-8"));
         int value = 0;

         for(int i = 0; i < result.length; i += 4) {
            int x = (result[i + 0] & 255) << 24 | (result[i + 1] & 255) << 16 | (result[i + 2] & 255) << 8 | result[i + 3] & 255;
            value ^= x;
         }

         return value;
      } catch (NoSuchAlgorithmException var6) {
         throw new FusionServerException("failed to get digest algorithm", AuthenticationServiceResponseCodeEnum.InternalError.value());
      } catch (UnsupportedEncodingException var7) {
         throw new FusionServerException("failed to get character set", AuthenticationServiceResponseCodeEnum.InternalError.value());
      }
   }

   private AuthenticationServiceResponseCodeEnum handleFusionCredentialAuthentication(Credential userCredential, String clientIP) throws FusionServerException {
      if (!(userCredential instanceof FusionCredential)) {
         log.error("passwordType == FUSION but we did not receive a FusionCredential");
         return AuthenticationServiceResponseCodeEnum.Failed;
      } else {
         FusionCredential fusionCredential = (FusionCredential)userCredential;

         try {
            int ratio = this.failedIPRatio(clientIP);
            if (ratio >= this.bruteForceIPRatio && ratio % 5000 == 0) {
               log.warn("brute force IP [" + clientIP + "], ratio = " + ratio);
            } else if (ratio > this.suspectIPRatio && ratio < this.bruteForceIPRatio && ratio % 5000 == 0) {
               log.warn("suspect IP [" + clientIP + "], ratio = " + ratio);
            }

            String clearText;
            if (fusionCredential.userID < 1) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.ENABLE_PRELOGIN_USERNAME_CHAR_VALIDATION)) {
                  try {
                     UsernameUtils.validateUsernameAsSearchKeyword(fusionCredential.username);
                  } catch (UsernameValidationException var10) {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.LOG_INVALID_USERNAME_ON_PRELOGIN) && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.LOG_INVALID_USERNAME_ON_PRELOGIN)) {
                        clearText = var10.getErrorCause() != null ? var10.getErrorCause().getCode() : "";
                        log.info("Received an invalid username.RemoteAddress:[" + clientIP + "].ErrorCode:[" + clearText + "].Username[" + MemCachedKeyUtils.getFullKeyFromStrings(fusionCredential.username) + "]");
                     }

                     return AuthenticationServiceResponseCodeEnum.InvalidCredential;
                  } catch (Exception var11) {
                     log.error("Unexpected Exception on handleFusionCredentialAuthentication", var11);
                     return AuthenticationServiceResponseCodeEnum.InternalError;
                  }
               }

               try {
                  fusionCredential.userID = this.userIDForFusionUsername(fusionCredential.username);
               } catch (FusionException var9) {
                  return AuthenticationServiceResponseCodeEnum.UnknownUsername;
               }
            }

            PersistedCredential loadedCredential = this.credentialDAO.getCredentialFromNewOrOldTable(fusionCredential.userID, PasswordType.FUSION, this.crypter);
            if (loadedCredential == null) {
               return AuthenticationServiceResponseCodeEnum.UnknownCredential;
            } else if (this.isCredentialExpired(loadedCredential)) {
               return AuthenticationServiceResponseCodeEnum.CredentialsExpired;
            } else {
               clearText = PasswordUtils.decryptPassword(loadedCredential, this.crypter);
               AtomicInteger failed;
               if (StringUtils.hasLength(fusionCredential.password)) {
                  if (!clearText.toLowerCase().equals(fusionCredential.password.toLowerCase())) {
                     failed = (AtomicInteger)this.failedBreakdown.putIfAbsent(clientIP, new AtomicInteger(1));
                     if (failed != null) {
                        failed.incrementAndGet();
                     }

                     return AuthenticationServiceResponseCodeEnum.Failed;
                  }
               } else {
                  String challenge = fusionCredential.loginChallenge + clearText;
                  if (challenge.hashCode() != fusionCredential.clientHash) {
                     challenge = fusionCredential.loginChallenge + clearText.toLowerCase();
                     if (challenge.hashCode() != fusionCredential.clientHash && getSha1HashCode(fusionCredential.loginChallenge + clearText) != fusionCredential.clientHash) {
                        AtomicInteger failed = (AtomicInteger)this.failedBreakdown.putIfAbsent(clientIP, new AtomicInteger(1));
                        if (failed != null) {
                           failed.incrementAndGet();
                        }

                        return AuthenticationServiceResponseCodeEnum.Failed;
                     }
                  }
               }

               failed = (AtomicInteger)this.succeededBreakdown.putIfAbsent(clientIP, new AtomicInteger(1));
               if (failed != null) {
                  failed.incrementAndGet();
               }

               return AuthenticationServiceResponseCodeEnum.Success;
            }
         } catch (KeyczarException var12) {
            log.error("failed to decrypt credentials for username [" + fusionCredential.username + "] while handling fusion authentication", var12);
            throw new FusionServerException("failed to decrypt credentials for username [" + fusionCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (EmptyResultDataAccessException var13) {
            log.error("there are no credentials for user [" + fusionCredential.username + "]", var13);
            return AuthenticationServiceResponseCodeEnum.UnknownCredential;
         } catch (DataAccessException var14) {
            log.error("failed to load credential for user [" + fusionCredential.username + "]", var14);
            throw new FusionServerException("failed to load credential for user [" + fusionCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   private AuthenticationServiceResponseCodeEnum handleMailCredentialAuthentication(Credential userCredential) throws FusionServerException {
      return StringUtils.hasLength(userCredential.username) && StringUtils.hasLength(userCredential.password) && userCredential.password.equals(this.surgeMailPassword) ? AuthenticationServiceResponseCodeEnum.Success : AuthenticationServiceResponseCodeEnum.Failed;
   }

   public AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential, Current __current) throws FusionException {
      ValidateCredentialResult validationResult = PasswordUtils.validateCredential(userCredential);
      if (!validationResult.valid) {
         return AuthenticationServiceResponseCodeEnum.InvalidCredential;
      } else {
         try {
            PersistedCredential credentialToBePersisted = PersistedCredential.fromCredentialSecured(this.crypter, userCredential, CredentialVersion.KEYCZAR);
            int id = this.credentialDAO.createCredential(credentialToBePersisted);
            if (id > 0) {
               return AuthenticationServiceResponseCodeEnum.Success;
            } else {
               log.error("failed to create credentials for user [" + userCredential.username + "] and pass type [" + userCredential.passwordType + "]");
               return AuthenticationServiceResponseCodeEnum.Failed;
            }
         } catch (KeyczarException var6) {
            log.error("failed to encrypt password for user [" + userCredential.username + "] creating credentials", var6);
            throw new FusionServerException("failed to encrypt credentials for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataIntegrityViolationException var7) {
            log.error("failed to persist credential for user [" + userCredential.username + "] and type [" + PasswordType.fromValue(userCredential.passwordType) + "], already exists?", var7);
            throw new FusionServerException("credentials already exists for username [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.CredentialAlreadyExists.value());
         } catch (DataAccessException var8) {
            log.error("failed to persist credential for user [" + userCredential.username + "]", var8);
            throw new FusionServerException("failed to persist credential for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential, Current __current) throws FusionException {
      ValidateCredentialResult validationResult = PasswordUtils.validateCredential(userCredential);
      if (!validationResult.valid) {
         return AuthenticationServiceResponseCodeEnum.InvalidCredential;
      } else if (userCredential.passwordType == PasswordType.FUSION.value()) {
         log.warn("username [" + userCredential.username + "] attempting to change password with updateCredential() must use updateFusionCredential()");
         throw new FusionBusinessException("Internal error", AuthenticationServiceResponseCodeEnum.InternalError.value());
      } else {
         try {
            PersistedCredential fusionCredential = this.credentialDAO.getCredential(userCredential.userID, PasswordType.FUSION);
            if (fusionCredential == null) {
               this.migrateUserCredentials(userCredential.userID);
            }

            PersistedCredential credentialToBePersisted = PersistedCredential.fromCredentialSecured(this.crypter, userCredential, CredentialVersion.KEYCZAR);
            int rowsUpdated = this.credentialDAO.updateCredentialPassword(credentialToBePersisted);
            if (rowsUpdated > 0) {
               return AuthenticationServiceResponseCodeEnum.Success;
            } else {
               log.error("failed to update credentials for user [" + userCredential.userID + "] and pass type [" + userCredential.passwordType + "], no rows found?");
               return AuthenticationServiceResponseCodeEnum.UnknownCredential;
            }
         } catch (KeyczarException var7) {
            log.error("failed to encrypt password for user [" + userCredential.username + "] when updating credentials", var7);
            throw new FusionServerException("failed to encrypt credentials for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var8) {
            log.error("failed to update credential for user [" + userCredential.username + "]", var8);
            throw new FusionServerException("failed to update credentials for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword, Current __current) throws FusionException {
      ValidateCredentialResult validationResult = PasswordUtils.validateCredential(userCredential);
      if (!validationResult.valid) {
         return AuthenticationServiceResponseCodeEnum.InvalidCredential;
      } else {
         try {
            if (userCredential.userID < 1) {
               userCredential.userID = this.userIDForFusionUsername(userCredential.username);
            }

            PersistedCredential fusionCredential = this.credentialDAO.getCredential(userCredential.userID, PasswordType.FUSION);
            if (fusionCredential == null) {
               CredentialList.deleteCredentialList(authenticationServiceMemCache, userCredential.userID);
               return AuthenticationServiceResponseCodeEnum.Success;
            } else if (oldPassword != null && !PasswordUtils.decryptPassword(fusionCredential, this.crypter).equals(oldPassword)) {
               log.warn("fusion username [" + userCredential.username + "] supplied incorrect old password when attempting to change password");
               throw new FusionBusinessException("Username and password do not match", AuthenticationServiceResponseCodeEnum.Failed.value());
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("new password = " + userCredential.password);
               }

               PersistedCredential credentialToBePersisted = PersistedCredential.fromCredentialSecured(this.crypter, userCredential, CredentialVersion.KEYCZAR);
               int rowsUpdated = this.credentialDAO.updateCredentialPassword(credentialToBePersisted);
               if (rowsUpdated > 0) {
                  return AuthenticationServiceResponseCodeEnum.Success;
               } else {
                  log.error("failed to update credentials for user [" + userCredential.userID + "] and pass type [" + userCredential.passwordType + "], no rows found?");
                  return AuthenticationServiceResponseCodeEnum.UnknownCredential;
               }
            }
         } catch (KeyczarException var8) {
            log.error("failed to encrypt password for fusion user [" + userCredential.username + "] when updating credentials", var8);
            throw new FusionServerException("failed to encrypt credentials for fusion user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var9) {
            log.error("failed to update credential for fusion user [" + userCredential.username + "]", var9);
            throw new FusionServerException("failed to update credentials for fusion user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   private PersistedCredential containsCredential(List<PersistedCredential> credentials, PasswordType passwordType) {
      if (credentials != null && !credentials.isEmpty() && passwordType != null) {
         Iterator i$ = credentials.iterator();

         PersistedCredential credential;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            credential = (PersistedCredential)i$.next();
         } while(credential.getPasswordType() != passwordType.value());

         return credential;
      } else {
         return null;
      }
   }

   private boolean removeCredential(List<PersistedCredential> credentials, PasswordType passwordType) {
      boolean removed = false;
      Iterator ite = credentials.iterator();

      while(ite.hasNext()) {
         PersistedCredential credential = (PersistedCredential)ite.next();
         if (credential.getPasswordType() == passwordType.value()) {
            ite.remove();
            removed = true;
         }
      }

      return removed;
   }

   private List<PersistedCredential> getExistingCredentialsOrMigrateOldCredentials(int userID) throws FusionException {
      List<PersistedCredential> existingCredentials = this.credentialDAO.getAllCredentials(userID);
      if (existingCredentials == null || existingCredentials.isEmpty()) {
         existingCredentials = this.migrateAndLoadUserCredentials(userID);
         if (existingCredentials == null || existingCredentials.isEmpty()) {
            log.error("failed to get existing or migrate credential for user [" + userID + "]");
            throw new FusionBusinessException("failed to get existing or migrate credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }

      return existingCredentials;
   }

   public AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential, Current __current) throws FusionException {
      if (userCredential != null && userCredential.userID >= 1 && userCredential.passwordType >= 1) {
         try {
            PersistedCredential credentialToBePersisted = PersistedCredential.fromCredentialSecured(this.crypter, userCredential, CredentialVersion.KEYCZAR);
            int rowsDeleted = this.credentialDAO.removeCredential(credentialToBePersisted);
            if (rowsDeleted > 0) {
               return AuthenticationServiceResponseCodeEnum.Success;
            } else {
               log.error("failed to remove credentials for user [" + userCredential.username + "] and pass type [" + userCredential.passwordType + "], no rows found?");
               return AuthenticationServiceResponseCodeEnum.UnknownCredential;
            }
         } catch (KeyczarException var5) {
            log.error("failed to encrypt password for user [" + userCredential.username + "] when removing credentials", var5);
            throw new FusionServerException("failed to encrypt credentials for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var6) {
            log.error("failed to remove credential for user [" + userCredential.username + "]", var6);
            throw new FusionServerException("failed to remove credentials for user [" + userCredential.username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      } else {
         return AuthenticationServiceResponseCodeEnum.InvalidCredential;
      }
   }

   public AuthenticationServiceResponseCodeEnum exists(int userID, byte passwordType, Current __current) throws FusionServerException {
      if (userID >= 1 && passwordType >= 1) {
         try {
            PersistedCredential persistedCredential = this.credentialDAO.getCredentialFromNewOrOldTable(userID, PasswordType.fromValue(passwordType), this.crypter);
            return persistedCredential == null ? AuthenticationServiceResponseCodeEnum.UnknownCredential : AuthenticationServiceResponseCodeEnum.Success;
         } catch (DataAccessException var5) {
            log.error("failed to load credential for user [" + userID + "]", var5);
            throw new FusionServerException("failed to load credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      } else {
         return AuthenticationServiceResponseCodeEnum.UnknownCredential;
      }
   }

   public AuthenticationServiceCredentialResponse getCredential(int userID, byte passwordType, Current __current) throws FusionException {
      if (userID >= 1 && passwordType >= 1) {
         try {
            PersistedCredential persistedCredential = this.credentialDAO.getCredentialFromNewOrOldTable(userID, PasswordType.fromValue(passwordType), this.crypter);
            if (log.isDebugEnabled()) {
               log.debug("found persisted cred for user id [" + userID + "]: " + persistedCredential);
            }

            return persistedCredential == null ? new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.UnknownCredential, (Credential)null) : new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.Success, persistedCredential.toUnSecuredCredential(this.crypter));
         } catch (KeyczarException var5) {
            log.error("failed to decrypt credentials for user [" + userID + "]", var5);
            throw new FusionServerException("failed to decrypt credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var6) {
            log.error("failed to load credential for user [" + userID + "]", var6);
            throw new FusionServerException("failed to load credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      } else {
         return new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.UnknownCredential, (Credential)null);
      }
   }

   public byte[] availableCredentialTypes(int userID, Current __current) throws FusionException {
      if (userID < 0) {
         return new byte[0];
      } else {
         try {
            Byte[] passwordTypes = this.credentialDAO.availableCredentialTypes(userID);
            if (log.isDebugEnabled()) {
               log.debug("found " + passwordTypes.length + " available types from DAO");
            }

            byte[] returnValue = new byte[passwordTypes.length];
            int index = 0;
            Byte[] arr$ = passwordTypes;
            int len$ = passwordTypes.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               byte value = arr$[i$];
               returnValue[index++] = value;
            }

            Arrays.sort(returnValue);
            if (log.isDebugEnabled()) {
               log.debug("sending back " + returnValue.length + " available types");
            }

            return returnValue;
         } catch (DataAccessException var10) {
            log.error("failed to load password types for user [" + userID + "]", var10);
            throw new FusionServerException("failed to load password types for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   public Credential[] getCredentialsForTypes(int userID, byte[] passwordTypes, Current __current) throws FusionException {
      if (userID < 1) {
         return new Credential[0];
      } else {
         if (log.isDebugEnabled()) {
            log.debug("finding credentials for userID " + userID);
         }

         try {
            List<PersistedCredential> persistedCredentials = this.credentialDAO.getCredentialsByTypes(userID, PasswordType.fromValues(passwordTypes));
            if (persistedCredentials != null && !persistedCredentials.isEmpty()) {
               int index = 0;
               Credential[] credentials = new Credential[persistedCredentials.size()];

               PersistedCredential persistedCredential;
               for(Iterator i$ = persistedCredentials.iterator(); i$.hasNext(); credentials[index++] = persistedCredential.toUnSecuredCredential(this.crypter)) {
                  persistedCredential = (PersistedCredential)i$.next();
               }

               return credentials;
            } else {
               return new Credential[0];
            }
         } catch (KeyczarException var9) {
            log.error("failed to decrypt credentials for user [" + userID + "] while getting fusion credentials for types", var9);
            throw new FusionServerException("failed to decrypt credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var10) {
            log.error("failed to load credential for user [" + userID + "]", var10);
            throw new FusionServerException("failed to load credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   public Credential[] getAllCredentials(int userID, Current __current) throws FusionException {
      if (userID < 1) {
         return new Credential[0];
      } else {
         try {
            List<PersistedCredential> persistedCredentials = this.credentialDAO.getAllCredentials(userID);
            if (persistedCredentials != null && !persistedCredentials.isEmpty()) {
               int index = 0;
               Credential[] credentials = new Credential[persistedCredentials.size()];

               PersistedCredential persistedCredential;
               for(Iterator i$ = persistedCredentials.iterator(); i$.hasNext(); credentials[index++] = persistedCredential.toUnSecuredCredential(this.crypter)) {
                  persistedCredential = (PersistedCredential)i$.next();
               }

               return credentials;
            } else {
               return new Credential[0];
            }
         } catch (KeyczarException var8) {
            log.error("failed to decrypt credentials for user [" + userID + "] while getting all credentials", var8);
            throw new FusionServerException("failed to decrypt credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var9) {
            log.error("failed to load credential for user [" + userID + "]", var9);
            throw new FusionServerException("failed to load credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   public Credential[] getAllCredentialsFromOldSource(int userID, Current __current) throws FusionException {
      if (userID < 1) {
         return new Credential[0];
      } else {
         try {
            List<PersistedCredential> persistedCredentials = this.credentialDAO.getAllCredentialsFromOldTable(userID);
            if (persistedCredentials != null && !persistedCredentials.isEmpty()) {
               int index = 0;
               Credential[] credentials = new Credential[persistedCredentials.size()];

               PersistedCredential persistedCredential;
               for(Iterator i$ = persistedCredentials.iterator(); i$.hasNext(); credentials[index++] = persistedCredential.toUnSecuredCredential(this.crypter)) {
                  persistedCredential = (PersistedCredential)i$.next();
               }

               return credentials;
            } else {
               return new Credential[0];
            }
         } catch (KeyczarException var8) {
            log.error("failed to decrypt credentials for user [" + userID + "] while getting all credentials from old source", var8);
            throw new FusionServerException("failed to decrypt credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         } catch (DataAccessException var9) {
            log.error("failed to load credential for user [" + userID + "]", var9);
            throw new FusionServerException("failed to load credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }
      }
   }

   private List<PersistedCredential> migrateAndLoadUserCredentials(int userID) throws FusionException {
      PersistedCredential credential;
      try {
         if (!MemCachedUtils.getLock(commonMemcache, "MU_NS", Integer.toString(userID), 15000)) {
            log.error("Failed to get a distributed lock in 15 seconds to migrate userID [" + userID + "]");
            throw new FusionServerException("failed to migrate user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
         }

         List<PersistedCredential> migratedCredentials = this.credentialDAO.getAllCredentialsFromMaster(userID);
         if (migratedCredentials != null && !migratedCredentials.isEmpty()) {
            Iterator i$ = migratedCredentials.iterator();

            while(i$.hasNext()) {
               credential = (PersistedCredential)i$.next();
               if (credential.getPasswordType() == PasswordType.FUSION.value()) {
                  log.warn("credentials for user [" + userID + "] were already migrated");
                  List var5 = migratedCredentials;
                  return var5;
               }
            }
         }

         log.info("migrating user [" + userID + "]");
         List<PersistedCredential> oldCredentials = this.credentialDAO.getAllCredentialsFromOldTable(userID);
         if (oldCredentials != null && !oldCredentials.isEmpty()) {
            try {
               Iterator i$ = oldCredentials.iterator();

               PersistedCredential credential;
               while(i$.hasNext()) {
                  credential = (PersistedCredential)i$.next();
                  credential.secure(this.crypter, CredentialVersion.KEYCZAR);
               }

               i$ = oldCredentials.iterator();

               while(i$.hasNext()) {
                  credential = (PersistedCredential)i$.next();
                  log.debug("secured cred " + credential.getUsername() + " " + credential.getPassword() + " " + PasswordType.fromValue(credential.getPasswordType()));
               }

               this.credentialDAO.createCredentials(oldCredentials);
               migratedCredentials = this.credentialDAO.getAllCredentialsFromMaster(userID);
               List var18 = migratedCredentials;
               return var18;
            } catch (DataIntegrityViolationException var12) {
               log.error("failed to persist credentials for user [" + userID + "] during migration", var12);
               throw new FusionServerException("failed to persist credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
            } catch (DataAccessException var13) {
               log.error("failed to read credentials for user [" + userID + "] during migration", var13);
               throw new FusionServerException("failed to read credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
            } catch (KeyczarException var14) {
               log.error("failed to encrypt credentials for user [" + userID + "] during migration", var14);
               throw new FusionServerException("failed to encrypt credentials for user [" + userID + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
            }
         }

         log.warn("no old credentials found for user [" + userID + "] to migrate");
         credential = null;
      } finally {
         MemCachedUtils.releaseLock(commonMemcache, "MU_NS", Integer.toString(userID));
      }

      return credential;
   }

   public void migrateUserCredentials(int userID, Current __current) throws FusionException {
      this.migrateAndLoadUserCredentials(userID);
   }

   public int userIDForFusionUsername(String username, Current __current) throws FusionException {
      try {
         return this.credentialDAO.userIDForUsername(username);
      } catch (DataAccessException var4) {
         throw new FusionServerException("failed to find user [" + username + "]", AuthenticationServiceResponseCodeEnum.InternalError.value());
      }
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType, Current __current) {
      List<PersistedCredential> credentials = this.credentialDAO.getCredentialsByUsernameAndPasswordType(username, PasswordType.fromValue(passwordType));
      if (credentials != null && credentials.size() != 0) {
         PersistedCredential latestCredential = null;
         if (credentials.size() == 1) {
            latestCredential = (PersistedCredential)credentials.get(0);
         } else {
            latestCredential = (PersistedCredential)credentials.remove(0);
            Iterator i$ = credentials.iterator();

            while(i$.hasNext()) {
               PersistedCredential credential = (PersistedCredential)i$.next();
               if (credential.getLastUpdated().after(latestCredential.getLastUpdated())) {
                  latestCredential = credential;
               }
            }
         }

         try {
            return new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.Success, latestCredential.toUnSecuredCredential(this.crypter));
         } catch (KeyczarException var8) {
            log.error("Unable to decrypt credential: " + var8);
            return new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.InternalError, (Credential)null);
         }
      } else {
         return new AuthenticationServiceCredentialResponse(AuthenticationServiceResponseCodeEnum.UnknownCredential, (Credential)null);
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential, Current __current) {
      if (userCredential == null) {
         return AuthenticationServiceResponseCodeEnum.InvalidCredential;
      } else {
         if (userCredential.userID == 0) {
            try {
               userCredential.userID = this.userIDForFusionUsername(userCredential.username);
            } catch (FusionException var4) {
               return AuthenticationServiceResponseCodeEnum.UnknownUsername;
            }
         }

         return this.checkCredential(userCredential.userID, userCredential.password, userCredential.passwordType);
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType, Current __current) {
      return this.checkCredential(userid, password, passwordType);
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType, Current __current) {
      int userID;
      try {
         userID = this.userIDForFusionUsername(username);
      } catch (FusionException var7) {
         return AuthenticationServiceResponseCodeEnum.UnknownUsername;
      }

      return this.checkCredential(userID, password, passwordType);
   }

   private AuthenticationServiceResponseCodeEnum checkCredential(int userid, String password, byte passwordType) {
      PersistedCredential persistedCredential = this.credentialDAO.getCredential(userid, PasswordType.fromValue(passwordType));
      if (persistedCredential == null) {
         return AuthenticationServiceResponseCodeEnum.UnknownCredential;
      } else {
         Credential cred;
         try {
            cred = persistedCredential.toUnSecuredCredential(this.crypter);
         } catch (KeyczarException var7) {
            return AuthenticationServiceResponseCodeEnum.InternalError;
         }

         if (cred == null) {
            return AuthenticationServiceResponseCodeEnum.InvalidCredential;
         } else {
            return password.equals(cred.password) ? AuthenticationServiceResponseCodeEnum.Success : AuthenticationServiceResponseCodeEnum.Failed;
         }
      }
   }

   static {
      commonMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.common);
      authenticationServiceMemCache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.authenticationService);
      CRYPTER_KEY_LOCATION = ConfigUtils.getConfigDirectory() + "/aeskeys";
   }
}
