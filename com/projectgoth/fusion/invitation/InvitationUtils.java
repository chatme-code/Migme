package com.projectgoth.fusion.invitation;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.invitation.restapi.data.SendingInvitationData;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class InvitationUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(InvitationUtils.class));
   private static final IvParameterSpec IVSPEC = new IvParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8});
   private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
   private static final String KEY_SPEC_ALGO = "AES";
   private static int MAX_REFERRAL_CODE_LENGTH = 32;
   public static final List<String> MIGBO_DESTINATION = Arrays.asList("migbo");

   public static void sendInvitationEmails(int userId, Map<String, Integer> emailIdToInvitationIdMap, SendingInvitationData data, InvitationData.ActivityType activityType) {
      try {
         String pathPrefix = String.format("/user/%d/referral?method=@email&confirmToInvitationEngine=%s", userId, SystemProperty.getBool("ReferralInvitationEnabled", true));
         JSONObject postData = new JSONObject();
         JSONObject emailReferralIds = new JSONObject();
         Iterator i$ = emailIdToInvitationIdMap.keySet().iterator();

         while(i$.hasNext()) {
            String email = (String)i$.next();
            emailReferralIds.put(email, encryptReferralInvitation((Integer)emailIdToInvitationIdMap.get(email)));
         }

         postData.put("destinations", emailReferralIds);
         if (activityType == InvitationData.ActivityType.JOIN_MIG33) {
            postData.put("invitationEmailType", 6);
         } else if (activityType == InvitationData.ActivityType.PLAY_A_GAME) {
            postData.put("invitationEmailType", 8);
            postData.put("thirdPartyAppID", data.invitationMetadata.gameId);
         } else if (activityType == InvitationData.ActivityType.GAME_HELP) {
            postData.put("invitationEmailType", 9);
            postData.put("thirdPartyAppID", data.invitationMetadata.gameId);
         } else if (activityType == InvitationData.ActivityType.SHARE_PROFILE) {
            postData.put("invitationEmailType", 12);
            postData.put("sharedUserID", data.invitationMetadata.sharedUserID);
         }

         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         apiUtil.postOneWay(pathPrefix, postData.toString());
      } catch (Exception var9) {
         log.error(var9);
      }

   }

   private static byte[] getReferralCipherKey() {
      return HashUtils.md5(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.REFERRAL_INVITATION_CIPHER_PASSPHRASE));
   }

   public static byte[] hexStringToByteArray(String s) {
      return StringUtil.hexStringToByteArray(s);
   }

   public static String encryptReferralInvitation(int invitationId) throws GeneralSecurityException, IOException {
      SecureRandom random = new SecureRandom();
      int randomNum = random.nextInt();
      SecretKeySpec keySpec = new SecretKeySpec(getReferralCipherKey(), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(1, keySpec, IVSPEC);
      ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
      DataOutputStream encryptStream = new DataOutputStream(new CipherOutputStream(baos, cipher));

      try {
         encryptStream.writeInt(invitationId);
         encryptStream.writeInt(randomNum);
      } finally {
         encryptStream.close();
      }

      byte[] cipherText = baos.toByteArray();
      if (log.isDebugEnabled()) {
         log.debug("invitationId:" + invitationId + ",encryptReferralInvitation:" + HashUtils.asHex(cipherText));
      }

      return HashUtils.asHex(cipherText);
   }

   public static int decryptReferralInvitation(String encryptedStr) {
      if (StringUtil.isBlank(encryptedStr)) {
         log.error("decryptReferralInvitation:blank encrypted string");
         return -1;
      } else if (encryptedStr.length() > MAX_REFERRAL_CODE_LENGTH) {
         log.error("decryptReferralInvitation:encrypted string too long");
         return -1;
      } else {
         int invitationId = -1;

         try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, new SecretKeySpec(getReferralCipherKey(), "AES"), IVSPEC);
            ByteArrayInputStream bais = new ByteArrayInputStream(hexStringToByteArray(encryptedStr));
            DataInputStream encryptStream = new DataInputStream(new CipherInputStream(bais, cipher));

            try {
               invitationId = encryptStream.readInt();
            } finally {
               encryptStream.close();
            }
         } catch (GeneralSecurityException var12) {
            log.error("decryptReferralInvitation:Failed to decrypt ", var12);
         } catch (IOException var13) {
            log.error("decryptReferralInvitation:Failed to decrypt ", var13);
         } catch (NumberFormatException var14) {
            log.error("decryptReferralInvitation:Failed to decrypt ", var14);
         }

         return invitationId;
      }
   }

   public static InvitationData.StatusFieldValue getInvitationStatus(InvitationData invitationData, Date timeOfAction) {
      if (invitationData == null) {
         return InvitationData.StatusFieldValue.INVALID;
      } else {
         if (invitationData.status == InvitationData.StatusFieldValue.NO_RESPONSE && invitationData.expireTime != null && timeOfAction.after(invitationData.expireTime)) {
            invitationData.status = InvitationData.StatusFieldValue.EXPIRED;
         }

         return invitationData.status;
      }
   }

   public static InvitationData.StatusFieldValue getNewInvitationStatusFieldValue(InvitationResponseData.ResponseType newResponseType, InvitationData invitationData) {
      if (invitationData.channel == InvitationData.ChannelType.EMAIL) {
         if (invitationData.type == InvitationData.ActivityType.JOIN_MIG33) {
            if (newResponseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED) {
               return InvitationData.StatusFieldValue.CLOSED;
            }

            return null;
         }
      } else if (invitationData.channel == InvitationData.ChannelType.FB && invitationData.type == InvitationData.ActivityType.JOIN_MIG33) {
         if (newResponseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED) {
            return InvitationData.StatusFieldValue.CLOSED;
         }

         return null;
      }

      throw new IllegalArgumentException("Unable to determine new invitation status field value when new response [" + newResponseType + "] for invitation " + invitationData);
   }

   public static void deduplicateEmails(Collection<String> emailCollectionSource, Collection<String> emailCollectionTarget) {
      HashSet<String> tmpTarget = new HashSet();

      String normalizedEmailAddr;
      for(Iterator i$ = emailCollectionSource.iterator(); i$.hasNext(); tmpTarget.add(normalizedEmailAddr)) {
         String emailAddr = (String)i$.next();
         if (StringUtil.isValidEmail(emailAddr)) {
            normalizedEmailAddr = StringUtil.normalizeEmailAddress(emailAddr);
         } else {
            normalizedEmailAddr = StringUtil.trimmedLowerCase(emailAddr);
         }
      }

      emailCollectionTarget.addAll(tmpTarget);
   }

   public static void deduplicateDestinations(SendingInvitationData sendingInvitationData) {
      if (InvitationData.ChannelType.fromTypeCode(sendingInvitationData.channel) == InvitationData.ChannelType.EMAIL) {
         ArrayList<String> uniqueDestinations = new ArrayList();
         deduplicateEmails(sendingInvitationData.destinations, uniqueDestinations);
         sendingInvitationData.destinations = uniqueDestinations;
      } else {
         Set<String> uniqueDestinations = new HashSet(sendingInvitationData.destinations);
         sendingInvitationData.destinations.clear();
         sendingInvitationData.destinations.addAll(uniqueDestinations);
      }

   }

   public static boolean isInvitationEngineEnabled(InvitationData.ChannelType channel) {
      if (channel != null || !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.INTERNAL_INVITATION_ENABLED) && !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.REFERRAL_INVITATION_ENABLED) && !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.FACEBOOK_INVITATION_ENABLED)) {
         return InvitationData.ChannelType.EMAIL == channel && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.REFERRAL_INVITATION_ENABLED) || InvitationData.ChannelType.FB == channel && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.FACEBOOK_INVITATION_ENABLED) || InvitationData.ChannelType.INTERNAL == channel && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.INTERNAL_INVITATION_ENABLED);
      } else {
         return true;
      }
   }

   public static boolean isUserMobileOrEmailVerified(UserData userData) {
      return userData != null && (userData.mobileVerified != null && userData.mobileVerified || userData.emailVerified != null && userData.emailVerified);
   }

   public static enum SendInvitationResultEnum implements EnumUtils.IEnumValueGetter<Integer> {
      SUCCESS_SEND_INVITATION(1),
      SEND_FOLLOWING_ME_REQUEST_TO_EXISTING_USER(2),
      NOTHING_HAPPENS_TO_HALFWAY_REGISTERED_USER(3),
      FAILED_TO_SEND_INVITATION_TO_DESTINATION(-1),
      INVALID_DESTINATION(-2);

      private static final Map<Integer, InvitationUtils.SendInvitationResultEnum> lookup = new HashMap();
      int value;

      private SendInvitationResultEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static InvitationUtils.SendInvitationResultEnum fromValue(int v) {
         return (InvitationUtils.SendInvitationResultEnum)lookup.get(v);
      }

      public Integer getEnumValue() {
         return this.value();
      }

      static {
         EnumUtils.populateLookUpMap(lookup, InvitationUtils.SendInvitationResultEnum.class);
      }
   }
}
