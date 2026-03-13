package com.projectgoth.fusion.recommendation.collector;

import Ice.Communicator;
import Ice.ObjectPrx;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.AppStartupInfo;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.recommendation.collector.addressbook.AddressBookDataLog4JSink;
import com.projectgoth.fusion.recommendation.collector.addressbook.CollectedAddressBookDataTransformer;
import com.projectgoth.fusion.recommendation.collector.rewardsystem.CollectedRewardProgramTriggerSummaryDataTransformer;
import com.projectgoth.fusion.recommendation.collector.rewardsystem.CollectedRewardProgramTriggerSummaryLog4jSink;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import com.projectgoth.fusion.slice.CollectedDataIce;
import com.projectgoth.fusion.slice.FusionExceptionWithRefCode;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServicePrx;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServicePrxHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class DataCollectorUtils {
   public static final Integer UNSPECIFIED_DATA_TYPE = -1;
   private static final IvParameterSpec IVSPEC = new IvParameterSpec(new byte[]{91, 82, 73, 64, 55, 46, 37, 28, 11, 92, 83, 74, 65, 56, 47, 38});
   private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
   private static final String KEY_SPEC_ALGO = "AES";
   public static final int UPLOAD_SIGNATURE_LENGTH = 32;
   public static final int MAX_UPLOADTICKET_REF_LENGTH = 128;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DataCollectorUtils.class));

   public static String newErrorID() {
      UUID errorID = UUID.randomUUID();
      String errorIDStr = errorID.toString().replaceAll("-", "");
      BigInteger bigIntErrorID = new BigInteger(errorIDStr, 16);
      return "E-" + bigIntErrorID.toString(36).toUpperCase();
   }

   public static Integer getDataType(CollectedDataIce data) {
      return data != null && data.dataType > 0 ? data.dataType : UNSPECIFIED_DATA_TYPE;
   }

   public static RecommendationDataCollectionServicePrx getRDCSProxy(Communicator communicator) throws IllegalStateException {
      String rdcsEndPoints = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.DATA_COLLECTION_SERVICE_ENDPOINTS);
      if (StringUtil.isBlank(rdcsEndPoints)) {
         String detailedMessage = SystemPropertyEntities.RecommendationServiceSettings.DATA_COLLECTION_SERVICE_ENDPOINTS.getName() + " not defined in the System Property";
         throw new IllegalStateException(detailedMessage);
      } else {
         ObjectPrx objectPrx = communicator.stringToProxy(rdcsEndPoints);
         boolean cachedConnection = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.ENABLE_RDCS_PROXY_WITH_CACHED_CONNECTION);
         return RecommendationDataCollectionServicePrxHelper.checkedCast(objectPrx.ice_connectionCached(cachedConnection));
      }
   }

   public static FusionExceptionWithRefCode prepareThrowingFusionExceptionWithRefCode(Logger log, String errorMessage, String additionalMessage, ErrorCause cause) {
      return prepareThrowingFusionExceptionWithRefCode(log, errorMessage, additionalMessage, cause, (Throwable)null);
   }

   public static FusionExceptionWithRefCode prepareThrowingFusionExceptionWithRefCode(Logger log, String errorMessage, ErrorCause cause) {
      return prepareThrowingFusionExceptionWithRefCode(log, errorMessage, (String)null, cause, (Throwable)null);
   }

   public static FusionExceptionWithRefCode prepareThrowingFusionExceptionWithRefCode(Logger log, String errorMessage, ErrorCause cause, Throwable causeByException) {
      return prepareThrowingFusionExceptionWithRefCode(log, errorMessage, (String)null, cause, causeByException);
   }

   public static FusionExceptionWithRefCode prepareThrowingFusionExceptionWithRefCode(Logger log, String errorMessage, String additionalLogMessage, ErrorCause errorCauseCode, Throwable cause) {
      StringBuilder logMessage = new StringBuilder();
      String errorRef = newErrorID();
      logMessage.append("ErrorRef:[").append(errorRef).append("].ErrorMessage:[").append(errorMessage).append("]");
      if (!StringUtil.isBlank(additionalLogMessage)) {
         logMessage.append("ExtraMessage:[").append(additionalLogMessage).append("]");
      }

      if (cause != null) {
         logMessage.append("CausedBy:[").append(cause).append("]");
         log.error(logMessage.toString(), cause);
      } else {
         log.error(logMessage.toString());
      }

      String errorCauseCodeStr = errorCauseCode == null ? "" : errorCauseCode.getCode();
      return new FusionExceptionWithRefCode(errorMessage, errorCauseCodeStr, errorRef);
   }

   private static byte[] getUploadTicketCipherKey(CollectedDataTypeEnum dataType) {
      return HashUtils.md5(SystemProperty.get(SystemPropertyEntities.RecommendationServiceSettings.UPLOAD_TICKET_CIPHER_CIPHER_PASSPHRASE.forCollectedDataType(dataType)));
   }

   public static byte[] getUploadHash(DataUploadRequest uploadRequest, int randomNum) throws DataCollectorException {
      StringBuilder stb = new StringBuilder(100);
      byte[] result = HashUtils.sha256(stb.append(uploadRequest.userid).append(uploadRequest.requestTimestamp).append(uploadRequest.dataType).append(randomNum).append(uploadRequest.sessionid).toString());
      if (result.length != 32) {
         log.error("Failed sanity check! Generated signature length is " + result.length + ". Expected " + 32);
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.INTERNAL_ERROR, new Object[0]);
      } else {
         return result;
      }
   }

   public static DataUploadTicket createUploadTicket(DataUploadRequest uploadRequest) throws DataCollectorException {
      SecureRandom random = new SecureRandom();
      int randomNum = random.nextInt();
      CollectedDataTypeEnum collectedDataType = CollectedDataTypeEnum.fromCode(uploadRequest.dataType);
      SecretKeySpec keySpec = new SecretKeySpec(getUploadTicketCipherKey(collectedDataType), "AES");

      try {
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(1, keySpec, IVSPEC);
         byte[] uploadHash = getUploadHash(uploadRequest, randomNum);
         ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
         DataOutputStream encryptStream = new DataOutputStream(new CipherOutputStream(baos, cipher));

         try {
            encryptStream.writeInt(uploadRequest.userid);
            encryptStream.writeLong(uploadRequest.requestTimestamp);
            encryptStream.writeInt(uploadRequest.dataType);
            encryptStream.writeInt(randomNum);
            encryptStream.write(uploadHash);
         } finally {
            encryptStream.close();
         }

         byte[] cipherText = baos.toByteArray();
         DataUploadTicket ticket = new DataUploadTicket();
         ticket.dataType = uploadRequest.dataType;
         ticket.random = randomNum;
         ticket.requestTimestamp = uploadRequest.requestTimestamp;
         ticket.signature = uploadHash;
         ticket.userid = uploadRequest.userid;
         ticket.ticketRef = HashUtils.asHex(cipherText);
         return ticket;
      } catch (GeneralSecurityException var16) {
         throw new DataCollectorException(var16, ErrorCause.DataCollectorErrorReasonType.TICKET_REF_CREATION_FAILURE, new Object[0]);
      } catch (IOException var17) {
         throw new DataCollectorException(var17, ErrorCause.DataCollectorErrorReasonType.TICKET_REF_CREATION_FAILURE, new Object[0]);
      } catch (Exception var18) {
         throw new DataCollectorException(var18, ErrorCause.DataCollectorErrorReasonType.TICKET_REF_CREATION_FAILURE, new Object[0]);
      }
   }

   public static DataUploadTicket decryptDataUploadTicket(String uploadTicketRef, String expectedSessionID, CollectedDataTypeEnum expectedCollectedDataType) throws DataCollectorException {
      if (StringUtil.isBlank(uploadTicketRef)) {
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.BLANK_UPLOAD_TICKET_REF, new Object[0]);
      } else if (uploadTicketRef.length() > 128) {
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.INVALID_UPLOAD_TICKET_REF, new Object[0]);
      } else {
         try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, new SecretKeySpec(getUploadTicketCipherKey(expectedCollectedDataType), "AES"), IVSPEC);
            ByteArrayInputStream bais = new ByteArrayInputStream(StringUtil.hexStringToByteArray(uploadTicketRef));
            DataInputStream decryptStream = new DataInputStream(new CipherInputStream(bais, cipher));

            DataUploadTicket var15;
            try {
               int userid = decryptStream.readInt();
               long requestTimestamp = decryptStream.readLong();
               int dataType = decryptStream.readInt();
               int random = decryptStream.readInt();
               byte[] receivedHash = new byte[32];
               decryptStream.readFully(receivedHash);
               DataUploadRequest dataUploadRequest = new DataUploadRequest();
               dataUploadRequest.userid = userid;
               dataUploadRequest.requestTimestamp = requestTimestamp;
               dataUploadRequest.dataType = dataType;
               dataUploadRequest.sessionid = expectedSessionID;
               byte[] expectedHash = getUploadHash(dataUploadRequest, random);
               if (!Arrays.equals(receivedHash, expectedHash)) {
                  throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.WRONG_UPLOAD_TICKET_SIGNATURE, new Object[0]);
               }

               DataUploadTicket uploadTicket = new DataUploadTicket();
               uploadTicket.dataType = dataType;
               uploadTicket.random = random;
               uploadTicket.requestTimestamp = requestTimestamp;
               uploadTicket.signature = receivedHash;
               uploadTicket.ticketRef = uploadTicketRef;
               uploadTicket.userid = userid;
               var15 = uploadTicket;
            } finally {
               decryptStream.close();
            }

            return var15;
         } catch (DataCollectorException var24) {
            throw var24;
         } catch (GeneralSecurityException var25) {
            throw new DataCollectorException(var25, ErrorCause.DataCollectorErrorReasonType.INVALID_UPLOAD_TICKET_REF, new Object[0]);
         } catch (IOException var26) {
            throw new DataCollectorException(var26, ErrorCause.DataCollectorErrorReasonType.CORRUPTED_TICKET_REF, new Object[0]);
         } catch (NumberFormatException var27) {
            throw new DataCollectorException(var27, ErrorCause.DataCollectorErrorReasonType.INVALID_UPLOAD_TICKET_REF, new Object[0]);
         }
      }
   }

   private static void checkUploadDataFeatureEnabled(CollectedDataTypeEnum collectedDataType) throws DataCollectorException {
      if (collectedDataType == null) {
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.UPLOAD_DATA_GLOBALLY_DISABLED, new Object[0]);
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.DATA_COLLECTION_SERVICE_ENABLED)) {
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.UPLOAD_DATA_GLOBALLY_DISABLED, new Object[0]);
      } else if (!SystemProperty.getBool(SystemPropertyEntities.RecommendationServiceSettings.DATA_COLLECTION_SERVICE_ENABLED.forCollectedDataType(collectedDataType))) {
         throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.UPLOAD_DATA_FOR_SPECIFIC_TYPE_DISABLED, new Object[]{collectedDataType});
      }
   }

   private static void checkUserAllowedToUploadAddressBook(int userId) throws DataCollectorException {
      int minMigLevel = SystemProperty.getInt(SystemPropertyEntities.RecommendationServiceSettings.UPLOAD_DATA_MIN_MIG_LEVEL.forCollectedDataType(CollectedDataTypeEnum.ADDRESSBOOKCONTACT));

      try {
         if (minMigLevel >= 1) {
            int userReputationLevel = MemCacheOrEJB.getUserReputationLevel((String)null, userId);
            if (userReputationLevel < minMigLevel) {
               throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.INSUFFICIENT_MIGLEVEL, new Object[]{minMigLevel});
            }
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GuardsetEnabled.UPLOAD_ADDRESSBOOK_DATA)) {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            boolean hasGuardCapability = userEJB.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.UPLOAD_ADDRESSBOOK_DATA.value());
            if (!hasGuardCapability) {
               throw new DataCollectorException(ErrorCause.DataCollectorErrorReasonType.INSUFFICIENT_RIGHTS, new Object[0]);
            }
         }

      } catch (RemoteException var4) {
         throw new DataCollectorException(var4, ErrorCause.DataCollectorErrorReasonType.INTERNAL_ERROR, new Object[0]);
      } catch (CreateException var5) {
         throw new DataCollectorException(var5, ErrorCause.DataCollectorErrorReasonType.INTERNAL_ERROR, new Object[0]);
      } catch (FusionEJBException var6) {
         throw new DataCollectorException(var6, ErrorCause.DataCollectorErrorReasonType.INTERNAL_ERROR, new Object[0]);
      }
   }

   public static void checkUserAccess(int userId, CollectedDataTypeEnum collectedDataType) throws DataCollectorException {
      checkUploadDataFeatureEnabled(collectedDataType);
      checkUserAllowedToUploadAddressBook(userId);
   }

   public static String getPerUserIdRLLocalKey(int userId) {
      return String.valueOf(userId);
   }

   public static String getPerUserIdPerCollectedDataTypeRLLocalKey(int userId, CollectedDataTypeEnum dataType) {
      if (dataType == null) {
         throw new IllegalArgumentException("dataType is null");
      } else {
         return MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(userId), String.valueOf(dataType.getCode()));
      }
   }

   public static void hitUploadDataRateLimit(int userId, CollectedDataTypeEnum collectedDataType) throws DataCollectorException {
      MemCachedRateLimiter.NameSpace nameSpace = MemCachedRateLimiter.NameSpace.UPLD_DATA;
      SystemPropertyEntities.RecommendationServiceSettings sysProp = SystemPropertyEntities.RecommendationServiceSettings.RATE_LIMIT_UPLOAD_DATA_PER_USER_ID;
      hitUploadRateLimit(nameSpace, getPerUserIdPerCollectedDataTypeRLLocalKey(userId, collectedDataType), sysProp.forCollectedDataType(collectedDataType));
      hitUploadRateLimit(nameSpace, getPerUserIdRLLocalKey(userId), sysProp);
      hitUploadRateLimit(nameSpace, "", SystemPropertyEntities.RecommendationServiceSettings.RATE_LIMIT_UPLOAD_DATA);
   }

   public static void hitUploadTicketRequestRateLimit(int userId, CollectedDataTypeEnum collectedDataType) throws DataCollectorException {
      MemCachedRateLimiter.NameSpace nameSpace = MemCachedRateLimiter.NameSpace.UPLD_TICKET_REQ;
      SystemPropertyEntities.RecommendationServiceSettings sysProp = SystemPropertyEntities.RecommendationServiceSettings.RATE_LIMIT_UPLOAD_TICKET_REQUEST_PER_USER_ID;
      hitUploadRateLimit(nameSpace, getPerUserIdPerCollectedDataTypeRLLocalKey(userId, collectedDataType), sysProp.forCollectedDataType(collectedDataType));
      hitUploadRateLimit(nameSpace, getPerUserIdRLLocalKey(userId), sysProp);
      hitUploadRateLimit(nameSpace, "", SystemPropertyEntities.RecommendationServiceSettings.RATE_LIMIT_UPLOAD_TICKET_REQUEST);
   }

   private static void hitUploadRateLimit(MemCachedRateLimiter.NameSpace rateLimitNameSpace, String key, SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntity) throws DataCollectorException {
      String hitPerDurationKey = sysPropEntity.getName();
      String hitPerDurationValue = SystemProperty.get(sysPropEntity);

      try {
         if (log.isDebugEnabled()) {
            log.debug("ns:" + rateLimitNameSpace + ";key:" + key + ";hitPerDurationValue:" + hitPerDurationValue);
         }

         MemCachedRateLimiter.hit(rateLimitNameSpace, key, hitPerDurationValue);
      } catch (MemCachedRateLimiter.LimitExceeded var6) {
         throw new DataCollectorException(var6, ErrorCause.DataCollectorErrorReasonType.RATE_LIMIT_BREACHED, new Object[]{hitPerDurationKey, hitPerDurationValue});
      } catch (MemCachedRateLimiter.FormatError var7) {
         throw new DataCollectorException(var7, ErrorCause.DataCollectorErrorReasonType.INVALID_RATE_LIMIT_CONFIG, new Object[]{hitPerDurationKey, hitPerDurationValue});
      }
   }

   public static DataCollectorContext createDefaultDataCollectorContext(AppStartupInfo appStartupInfo) {
      DataCollectorContext ctx = new DataCollectorContext(appStartupInfo);
      ctx.addStrategy(CollectedDataTypeEnum.ADDRESSBOOKCONTACT.getCode(), (new DataCollectorWriterStrategy("addressbookcontacts-stgy", new CollectedAddressBookDataTransformer("addressbookcontacts-xformer"))).addSink(new AddressBookDataLog4JSink("addressbookcontacts-sink-log4j", "addressbookcontacts-logger")));
      ctx.addStrategy(CollectedDataTypeEnum.REWARD_PROGRAM_TRIGGER_SUMMARY.getCode(), (new DataCollectorWriterStrategy("rewardprogramtriggersummary-stgy", new CollectedRewardProgramTriggerSummaryDataTransformer("rewardprogramtriggersummary-xformer"))).addSink(new CollectedRewardProgramTriggerSummaryLog4jSink("rewardprogramtriggersummary-sink-log4j", "rewardprogramtriggersummary-logger")));
      return ctx;
   }

   public static long sumStringLength(String[] strArray) {
      long total = 0L;
      if (strArray != null) {
         String[] arr$ = strArray;
         int len$ = strArray.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String str = arr$[i$];
            total += str == null ? 0L : (long)str.length();
         }
      }

      return total;
   }
}
