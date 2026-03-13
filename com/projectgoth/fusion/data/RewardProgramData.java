package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.leto.common.impl.outcome.ItemRewardMethodType;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jboss.logging.Logger;

public class RewardProgramData implements Serializable {
   public static final String DEFAULT_TMPLT_DATA_REWARD_PROGRAM_ID = "N/A";
   public static final String DEFAULT_TMPLT_DATA_REWARD_PROGRAM_NAME = "N/A";
   public static final String DEFAULT_TMPLT_DATA_REWARD_PROGRAM_DESCRIPTION = "N/A";
   public static final String DEFAULT_TMPLT_DATA_REWARD_PROGRAM_END_DATE = "(No end date)";
   public static final String DEFAULT_TMPLT_DATA_REWARD_PROGRAM_START_DATE = "(No start date)";
   public static final String STR_ZERO = "0";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardProgramData.class));
   public static final String TMPL_DATA_KEY_REWARDPROGRAM_PARAM_PREFIX = "rewardprogram.param";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_BADGE_REWARDS_SIZE = "rewardprogram.badgeRewardsSize";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_GROUP_MEMBERSHIP_REWARDS_SIZE = "rewardprogram.groupMembershipRewardsSize";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_STORE_ITEM_REWARDS_SIZE = "rewardprogram.storeItemRewardsSize";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_END_DATE = "rewardprogram.endDate";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_START_DATE = "rewardprogram.startDate";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_MIG_CREDIT_REWARD_CURRENCY = "rewardprogram.migCreditRewardCurrency";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_MIG_CREDIT_REWARD = "rewardprogram.migCreditReward";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_LEVEL_REWARD = "rewardprogram.levelReward";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_SCORE_REWARD = "rewardprogram.scoreReward";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_DESCRIPTION = "rewardprogram.description";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_NAME = "rewardprogram.name";
   public static final String TMPLT_DATA_KEY_REWARDPROGRAM_ID = "rewardprogram.id";
   private static final String START_MARKER_OF_SET_DATA = "SET:{";
   private static final String END_MARKER_OF_SET_DATA = "}";
   private static final char ELEMENT_DELIMITER = ';';
   public Integer id;
   public String name;
   public String description;
   public Integer countryID;
   public Integer minMigLevel;
   public Integer maxMigLevel;
   public RewardProgramData.TypeEnum type;
   public RewardProgramData.CategoryEnum category;
   public RewardProgramData.RewardFrequencyEnum rewardFrequency;
   public Integer quantityRequired;
   public Double amountRequired;
   public String amountRequiredCurrency;
   public String completionRateLimit;
   public List<Integer> storeItemRewards = new LinkedList();
   public List<Integer> groupMembershipRewards = new LinkedList();
   public Integer scoreReward;
   public Integer levelReward;
   public Double migCreditReward;
   public String migCreditRewardCurrency;
   public List<Integer> badgeRewards = new LinkedList();
   public String imNotification;
   public String emailNotification;
   public String smsNotification;
   public Date startDate;
   public Date endDate;
   public RewardProgramData.StatusEnum status;
   public RewardProgramData.ItemRewardType itemRewardType;
   public Integer emailTemplateID;
   public String emailTemplateDataProviderClassName;
   public UserData.TypeEnum userType;
   private Map<String, Collection<String>> parameters = new HashMap();
   private List<String> outcomeProcessorClassNames = Collections.emptyList();
   private final List<StoreItemToUnlockData> storeItemToUnlockRewards = new ArrayList();
   private final List<StoreItemToUnlockData> readOnlyStoreItemToUnlockRewards;
   private int merchantRewardPoints;
   private String rewardProgramStateHandlerClassFullPath;
   public static final int MIN_MIG_LEVEL_FOR_REWARD_PROGRAM = 1;

   public boolean needToCheckUserReputation() {
      return this.minMigLevel != null && this.minMigLevel > 1 || this.maxMigLevel != null;
   }

   public RewardProgramData() {
      this.readOnlyStoreItemToUnlockRewards = Collections.unmodifiableList(this.storeItemToUnlockRewards);
      this.merchantRewardPoints = 0;
   }

   public RewardProgramData(ResultSet rs) throws SQLException {
      this.readOnlyStoreItemToUnlockRewards = Collections.unmodifiableList(this.storeItemToUnlockRewards);
      this.merchantRewardPoints = 0;
      this.id = (Integer)rs.getObject("id");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.countryID = (Integer)rs.getObject("countryID");
      this.quantityRequired = (Integer)rs.getObject("quantityRequired");
      this.amountRequired = (Double)rs.getObject("amountRequired");
      this.amountRequiredCurrency = rs.getString("amountRequiredCurrency");
      this.scoreReward = (Integer)rs.getObject("scoreReward");
      this.levelReward = (Integer)rs.getObject("levelReward");
      this.migCreditReward = (Double)rs.getObject("migCreditReward");
      this.migCreditRewardCurrency = rs.getString("migCreditRewardCurrency");
      this.imNotification = rs.getString("imNotification");
      this.emailNotification = rs.getString("emailNotification");
      this.smsNotification = rs.getString("smsNotification");
      this.startDate = rs.getTimestamp("startDate");
      this.endDate = rs.getTimestamp("endDate");
      this.minMigLevel = (Integer)rs.getObject("MinMigLevel");
      this.maxMigLevel = (Integer)rs.getObject("MaxMigLevel");
      this.rewardProgramStateHandlerClassFullPath = rs.getString("rewardprogramstatehandlerclassname");
      Integer intval = (Integer)rs.getObject("type");
      if (intval != null) {
         this.type = RewardProgramData.TypeEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("rewardFrequency");
      if (intval != null) {
         this.rewardFrequency = RewardProgramData.RewardFrequencyEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("status");
      if (intval != null) {
         this.status = RewardProgramData.StatusEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("itemRewardType");
      if (intval != null) {
         this.itemRewardType = RewardProgramData.ItemRewardType.fromValue(intval);
      }

      intval = (Integer)rs.getObject("userType");
      if (intval != null) {
         this.userType = UserData.TypeEnum.fromValue(intval);
      } else {
         this.userType = null;
      }

      this.completionRateLimit = rs.getString("MaxCompletionRate");
      intval = (Integer)rs.getObject("category");
      if (intval != null) {
         this.category = RewardProgramData.CategoryEnum.fromValue(intval);
      }

      this.emailTemplateID = (Integer)rs.getObject("emailtemplateid");
      this.emailTemplateDataProviderClassName = rs.getString("emailtemplatedataprovider");
      this.merchantRewardPoints = rs.getInt("merchrewardpoints");
      if (rs.wasNull()) {
         this.merchantRewardPoints = 0;
      }

   }

   public String completionKey(int userID) {
      return this.id + "/c/" + userID;
   }

   public String quantityKey(int userID) {
      return this.id + "/q/" + userID;
   }

   public String valueKey(int userID) {
      return this.id + "/v/" + userID;
   }

   public String dispatchKey() {
      return this.id + "/d";
   }

   /** @deprecated */
   public boolean isActive(int countryID, int migLevel, UserData.TypeEnum userType) {
      if (this.status != RewardProgramData.StatusEnum.ACTIVE) {
         return false;
      } else if (this.userType != null && this.userType != userType) {
         return false;
      } else if (this.countryID != null && this.countryID != countryID) {
         return false;
      } else if (this.minMigLevel != null && migLevel < this.minMigLevel || this.maxMigLevel != null && migLevel > this.maxMigLevel) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         return now > this.startDate.getTime() && (this.endDate == null || now < this.endDate.getTime());
      }
   }

   public void setParameters(Map<String, String> params) {
      this.parameters = new HashMap();
      Iterator i$ = params.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, String> e = (Entry)i$.next();
         String key = (String)e.getKey();
         String strVal = (String)e.getValue();
         String strValToUpper = strVal.toUpperCase();
         if (strValToUpper.startsWith("SET:{")) {
            if (!strValToUpper.endsWith("}")) {
               throw new RewardProgramData.IllegalParameterException("Invalid 'SET' format [" + strVal + "]");
            }

            HashSet<String> value = new HashSet();
            value.addAll(StringUtil.split(strVal.substring("SET:{".length(), strVal.length() - "}".length()), ';'));
            if (log.isDebugEnabled()) {
               log.debug(String.format("programId:[%s] key:[%s] set-val:[%s]", this.id, key, value));
            }

            this.parameters.put(key, value);
         } else {
            List<String> strValList = StringUtil.split(strVal, ';');
            if (strValList != null) {
               if (strValList.size() == 1) {
                  this.parameters.put(key, Arrays.asList(strVal));
               } else {
                  this.parameters.put(key, strValList);
               }

               if (log.isDebugEnabled()) {
                  log.debug(String.format("programId:[%s] key:[%s] list-val:[%s]", this.id, key, strValList));
               }
            }
         }
      }

   }

   public void setOutcomeProcessorClasses(List<String> processorClassNames) {
      if (processorClassNames != null) {
         this.outcomeProcessorClassNames = processorClassNames;
      }

   }

   public List<String> getOutcomeProcessorClassNames() {
      return this.outcomeProcessorClassNames;
   }

   public int getIntParam(String key, int defaultValue) {
      String valueStr = this.castParamToString(key);
      return StringUtil.toIntOrDefault(valueStr, defaultValue);
   }

   public double getDoubleParam(String key, double defaultValue) {
      String valueStr = this.castParamToString(key);
      return StringUtil.toDoubleOrDefault(valueStr, defaultValue);
   }

   public List<String> getStringListParam(String paramName) {
      Collection<String> coll = (Collection)this.parameters.get(paramName);
      if (coll == null) {
         return Collections.EMPTY_LIST;
      } else if (coll instanceof Set) {
         return new ArrayList(coll);
      } else if (coll instanceof List) {
         return (List)coll;
      } else {
         throw new IllegalStateException("ProgramID:[" + this.id + "] Parameter:[" + paramName + "] type is unexpected [" + coll.getClass() + "]");
      }
   }

   public boolean getBoolParam(String key, boolean defaultValue) {
      String valueStr = this.castParamToString(key);
      return StringUtil.toBooleanOrDefault(valueStr, defaultValue);
   }

   public long getLongParam(String key, long defaultValue) {
      String valueStr = this.castParamToString(key);
      return StringUtil.toLongOrDefault(valueStr, defaultValue);
   }

   public String getStringParam(String key, String defaultValue) {
      String valueStr = this.castParamToString(key);
      return valueStr == null ? defaultValue : valueStr.trim();
   }

   public boolean isReputationMechanic() {
      return this.scoreReward != null && this.category != null && this.scoreReward > 0 && (this.category == RewardProgramData.CategoryEnum.REPUTATION_MONETIZATION || this.category == RewardProgramData.CategoryEnum.REPUTATION_NON_MONETIZATION);
   }

   public Set<String> getStringSetParam(String key) {
      Collection<String> coll = (Collection)this.parameters.get(key);
      if (coll == null) {
         return Collections.EMPTY_SET;
      } else {
         return (Set)(coll instanceof Set ? (Set)coll : new HashSet(coll));
      }
   }

   private String castParamToString(String paramName) {
      Collection<String> coll = (Collection)this.parameters.get(paramName);
      if (coll == null) {
         return null;
      } else if (coll instanceof Set) {
         throw new RewardProgramData.IncompatibleTypeException("ProgramID:[" + this.id + "] Parameter:[" + paramName + "] is a set");
      } else if (coll instanceof List) {
         if (coll != null && coll.size() != 0) {
            if (coll.size() == 1) {
               return (String)((List)coll).get(0);
            } else {
               throw new RewardProgramData.IncompatibleTypeException("ProgramID:[" + this.id + "] Parameter:[" + paramName + "] is a multi valued list");
            }
         } else {
            return null;
         }
      } else {
         throw new IllegalStateException("ProgramID:[" + this.id + "] Parameter:[" + paramName + "] type is unexpected [" + coll.getClass() + "]");
      }
   }

   public boolean hasParameter(String paramName) {
      return this.parameters.containsKey(paramName);
   }

   private static String getCollectionSizeAsString(Collection<?> coll) {
      return coll == null ? "0" : String.valueOf(coll.size());
   }

   private static String getNumberAsString(Number number) {
      return number == null ? "0" : number.toString();
   }

   public final void populateTemplateDataMap(Map<String, String> templateDataMap) {
      templateDataMap.put("rewardprogram.id", StringUtil.toStringOrDefault(this.id, "N/A"));
      templateDataMap.put("rewardprogram.name", StringUtil.toStringOrDefault(this.name, "N/A"));
      templateDataMap.put("rewardprogram.description", StringUtil.toStringOrDefault(this.description, "N/A"));
      templateDataMap.put("rewardprogram.scoreReward", getNumberAsString(this.scoreReward));
      templateDataMap.put("rewardprogram.levelReward", getNumberAsString(this.levelReward));
      templateDataMap.put("rewardprogram.migCreditReward", getNumberAsString(this.migCreditReward));
      templateDataMap.put("rewardprogram.migCreditRewardCurrency", StringUtil.toStringOrDefault(this.migCreditRewardCurrency, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DEFAULT_CURRENCY)));
      if (this.startDate != null) {
         templateDataMap.put("rewardprogram.startDate", DateTimeUtils.getStringForMigcore(this.startDate));
      } else {
         templateDataMap.put("rewardprogram.startDate", "(No start date)");
      }

      if (this.endDate != null) {
         templateDataMap.put("rewardprogram.endDate", DateTimeUtils.getStringForMigcore(this.endDate));
      } else {
         templateDataMap.put("rewardprogram.endDate", "(No end date)");
      }

      templateDataMap.put("rewardprogram.storeItemRewardsSize", getCollectionSizeAsString(this.storeItemRewards));
      templateDataMap.put("rewardprogram.groupMembershipRewardsSize", getCollectionSizeAsString(this.groupMembershipRewards));
      templateDataMap.put("rewardprogram.badgeRewardsSize", getCollectionSizeAsString(this.badgeRewards));
      Iterator i$ = this.parameters.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Collection<String>> e = (Entry)i$.next();
         if (((Collection)e.getValue()).size() == 1) {
            String value = (String)((Collection)e.getValue()).iterator().next();
            templateDataMap.put("rewardprogram.param[" + (String)e.getKey() + "]", value);
         }
      }

   }

   public boolean matchesDateConstraint(String dateConstraintParamKey, boolean dateConstraintParamKeyIsLowerBoundConstraint, Date dateToCheck) {
      String dateConstraintStr = this.getStringParam(dateConstraintParamKey, (String)null);
      if (!StringUtil.isBlank(dateConstraintStr)) {
         if (dateToCheck == null) {
            if (log.isDebugEnabled()) {
               log.debug(String.format("RewardProgramID:[%s] dateToCheck is null", this.id, dateToCheck));
            }

            return false;
         } else {
            Date _dateConstraint;
            try {
               _dateConstraint = DateTimeUtils.getUTCDateTime(dateConstraintStr);
            } catch (ParseException var10) {
               try {
                  _dateConstraint = DateTimeUtils.getUTCDate(dateConstraintStr);
               } catch (ParseException var9) {
                  log.error(String.format("Incorrect date time format RewardProgramID:[%s] dateConstraintParamKey:[%s] dateConstraintStr:[%s] ", this.id, dateConstraintParamKey, dateConstraintStr), var10);
                  log.error(String.format("Incorrect date time format RewardProgramID:[%s] dateConstraintParamKey:[%s] dateConstraintStr:[%s] ", this.id, dateConstraintParamKey, dateConstraintStr), var9);
                  return false;
               }
            }

            Date dateConstraint = _dateConstraint;
            boolean passed = dateConstraintParamKeyIsLowerBoundConstraint ? _dateConstraint.getTime() <= dateToCheck.getTime() : _dateConstraint.getTime() >= dateToCheck.getTime();
            if (log.isDebugEnabled()) {
               log.debug(String.format("RewardProgramID:[%s] dateConstraintParamKey:[%s] dateConstraint:[%s] dateToCheck:[%s] dateConstraintParamKeyIsLowerBoundConstraint:[%s] passed:[%s]", this.id, dateConstraintParamKey, dateConstraint, dateToCheck, dateConstraintParamKeyIsLowerBoundConstraint, passed));
            }

            return passed;
         }
      } else {
         return true;
      }
   }

   public boolean matchesSetOfStringsConstraint(String setOfStringsFieldParamKey, String setOfStringsIsWhiteListParamKey, String strToCheck) {
      if (this.hasParameter(setOfStringsFieldParamKey)) {
         boolean expectsToExistInSet = this.getBoolParam(setOfStringsIsWhiteListParamKey, true);
         Set<String> stringSet = this.getStringSetParam(setOfStringsFieldParamKey);
         boolean strToCheckInSet = stringSet != null ? stringSet.contains(strToCheck) : false;
         boolean passed = expectsToExistInSet ? strToCheckInSet : !strToCheckInSet;
         if (log.isDebugEnabled()) {
            log.debug(String.format("RewardProgramID:[%s] stringsSetParamKey:[%s] stringsSet:[%s] itemToCheck:[%s] stringsSetIsWhitelist:[%s] passed:[%s]", this.id, setOfStringsFieldParamKey, stringSet, strToCheck, setOfStringsIsWhiteListParamKey, passed));
         }

         return passed;
      } else {
         return true;
      }
   }

   public boolean matchesRegExKey(String regexKey, String strToCheck) throws PatternSyntaxException {
      String regex = this.getStringParam(regexKey, (String)null);
      if (log.isDebugEnabled()) {
         log.debug(String.format("RewardProgramID:[%s] regexkey:[%s] regex:[%s] strToCheck:[%s]", this.id, regexKey, regex, strToCheck));
      }

      boolean passed;
      if (regex == null) {
         passed = true;
      } else if (StringUtil.isBlank(regex)) {
         passed = strToCheck != null && StringUtil.isBlank(strToCheck);
      } else {
         try {
            passed = strToCheck != null && Pattern.matches(regex, strToCheck);
         } catch (PatternSyntaxException var6) {
            log.error(String.format("Incorrect regex RewardProgramID:[%s] regexkey:[%s] regex:[%s] input:[%s]", this.id, regexKey, regex, strToCheck), var6);
            passed = false;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug(String.format("RewardProgramID:[%s] regexkey:[%s] regex:[%s] input:[%s] passed:[%s]", this.id, regexKey, regex, strToCheck, passed));
      }

      return passed;
   }

   public boolean matchesVerifiedAccountStatusConstraint(String verifiedStatusConstraintParamKey, UserData.AccountVerifiedEnum accountVerifiedStatus) {
      if (this.hasParameter(verifiedStatusConstraintParamKey)) {
         int expectedVerifiedAccountStatusCode = this.getIntParam(verifiedStatusConstraintParamKey, UserData.AccountVerifiedEnum.PENDING.value());
         int accountVerifiedStatusStatusCode = accountVerifiedStatus == null ? UserData.AccountVerifiedEnum.PENDING.value() : accountVerifiedStatus.value();
         return expectedVerifiedAccountStatusCode == accountVerifiedStatusStatusCode;
      } else {
         return true;
      }
   }

   public boolean matchesVerifiedAccountTypeConstraint(String verifiedAccountTypeConstraintParamKey, UserData.AccountTypeEnum verifiedAccountType) {
      if (this.hasParameter(verifiedAccountTypeConstraintParamKey)) {
         int expectedVerifiedAccountEntityTypeCode = this.getIntParam(verifiedAccountTypeConstraintParamKey, UserData.AccountTypeEnum.INDIVIDUAL.value());
         int verifiedAccountEntityTypeCode = verifiedAccountType == null ? UserData.AccountTypeEnum.INDIVIDUAL.value() : verifiedAccountType.value();
         return expectedVerifiedAccountEntityTypeCode == verifiedAccountEntityTypeCode;
      } else {
         return true;
      }
   }

   public void addToStoreItemToUnlockRewards(int unlockedStoreItemID, int unlockedStoreItemQuantity) {
      this.storeItemToUnlockRewards.add(new StoreItemToUnlockData(unlockedStoreItemID, unlockedStoreItemQuantity));
   }

   public List<StoreItemToUnlockData> getStoreItemToUnlockRewards() {
      return this.readOnlyStoreItemToUnlockRewards;
   }

   public int getMerchantRewardPoints() {
      return this.merchantRewardPoints;
   }

   public void setRewardProgramStateHandlerClassName(String rewardProgramStateHandlerClassFullPath) {
      this.rewardProgramStateHandlerClassFullPath = rewardProgramStateHandlerClassFullPath;
   }

   public String getRewardProgramStateHandlerClassFullPath() {
      return this.rewardProgramStateHandlerClassFullPath;
   }

   public Set<String> getParameterNames() {
      return Collections.unmodifiableSet(this.parameters.keySet());
   }

   public static class IncompatibleTypeException extends RuntimeException {
      IncompatibleTypeException(String msg) {
         super(msg);
      }
   }

   public static class IllegalParameterException extends RuntimeException {
      IllegalParameterException(String msg) {
         super(msg);
      }
   }

   public static enum ItemRewardType {
      ALL(ItemRewardMethodType.ALL.typeId),
      RANDOM(ItemRewardMethodType.RANDOM.typeId);

      private int value;

      private ItemRewardType(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RewardProgramData.ItemRewardType fromValue(int value) {
         RewardProgramData.ItemRewardType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RewardProgramData.ItemRewardType e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum CategoryEnum {
      NO_CATEGORY(0),
      REPUTATION_NON_MONETIZATION(1),
      REPUTATION_MONETIZATION(2);

      private int value;

      private CategoryEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RewardProgramData.CategoryEnum fromValue(int value) {
         RewardProgramData.CategoryEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RewardProgramData.CategoryEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RewardProgramData.StatusEnum fromValue(int value) {
         RewardProgramData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RewardProgramData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum RewardFrequencyEnum {
      ONCE_OFF(1),
      REPEATING(2);

      private int value;

      private RewardFrequencyEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RewardProgramData.RewardFrequencyEnum fromValue(int value) {
         RewardProgramData.RewardFrequencyEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RewardProgramData.RewardFrequencyEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum implements EnumUtils.IEnumValueGetter<Integer> {
      MANUAL(0),
      USER_REFERRAL_AUTHENTICATED(1),
      VIRTUAL_GIFT_SENT(2),
      VIRTUAL_GIFT_RECEIVED(3),
      AVATAR_PURCHASED(4),
      GAME_ITEM_PURCHASED(5),
      MIG_LEVEL(6),
      GROUP_CREATED(7),
      GROUP_INVITATION_SENT(8),
      SMS_SENT(9),
      PHONE_CALL_MADE(10),
      EMAIL_SENT(11),
      FUSION_PRIVATE_MESSAGE_SENT(12),
      FUSION_PRIVATE_MESSAGE_RECEIVED(13),
      MSN_SENT(14),
      MSN_RECEIVED(15),
      YAHOO_SENT(16),
      YAHOO_RECEIVED(17),
      AIM_SENT(18),
      AIM_RECEIVED(19),
      GTALK_SENT(20),
      GTALK_RECEIVED(21),
      FACEBOOK_SENT(22),
      FACEBOOK_RECEIVED(23),
      MIGBO_POST_CREATED(24),
      CONSECUTIVE_LOGIN(25),
      LAST_LOGIN(26),
      BOT_GAME_WON(27),
      THIRDPARTY_APP_PURCHASE(28),
      CREDIT_RECHARGE(29),
      MERCHANT_TRAILS_EARNED(30),
      BOT_GAME_SPENDING(31),
      PHOTOS_UPLOADED(32),
      FUSION_CHATROOM_MESSAGES_SENT(33),
      GROUP_TOPIC_CREATED(34),
      GROUP_TOPIC_COMMENT_CREATED(35),
      GROUP_WALLPOST_CREATED(36),
      GROUP_WALLPOST_COMMENT_CREATED(37),
      THIRDPARTY_APP_START(38),
      THIRDPARTY_APP_INTERNAL_INVITATION_SENT(39),
      THIRDPARTY_APP_INTERNAL_INVITATION_ACCEPTED(40),
      MIGBO_CAMPAIGN_EVENT(41),
      USER_FIRST_AUTHENTICATED(42),
      INVITATION_RESPONDED(43),
      MIGBO_FOLLOWING_EVENT(44),
      MIGBO_POST_EVENT(45),
      REFERRED_USER_MEETS_REWARD_CRITERIA(46),
      REFERRED_USER_REWARDED_WITH_MIGLEVEL(47),
      REFERRED_USER_REWARDED_WITH_MIGCREDITS(48),
      REFERRED_USER_REWARDED_WITH_BADGES(49),
      REFERRED_USER_REWARDED_WITH_GROUPMEMBERSHIP(50),
      REFERRED_USER_REWARDED_WITH_STOREITEM(51),
      EXTERNAL_EMAIL_VERIFIED_EVENT(52),
      MERCHANT_TAGGED_USER_MEETS_REWARD_CRITERIA(53),
      MIGBO_FOLLOWED_BY_EVENT(54),
      MUTUALLY_FOLLOWING_EVENT(55),
      USER_MEETS_REWARD_CRITERIA(56),
      MMV2(57),
      USER_REWARDED_WITH_REPUTATION_SCORE(58),
      USER_REWARDED_WITH_STOREITEM(59),
      USER_REWARDED_WITH_UNLOCKED_STOREITEM(60),
      USER_REWARDED_WITH_BADGE(61),
      USER_REWARDED_WITH_MIGCREDITS(62),
      REFERRED_USER_REWARDED_WITH_UNLOCKED_STOREITEM(63),
      TEST_EVENT_1000(1000),
      TEST_EVENT_1001(1001),
      TEST_EVENT_1002(1002),
      TEST_EVENT_1003(1003),
      TEST_EVENT_1004(1004),
      SIMPLE_SUBJECT_TRIGGERED_EVENT(1100);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RewardProgramData.TypeEnum fromValue(int value) {
         return (RewardProgramData.TypeEnum)RewardProgramData.TypeEnum.Singletons.getInstance().getIntToTypeEnumMap().get(value);
      }

      public Integer getEnumValue() {
         return this.value;
      }

      public int getId() {
         return this.value;
      }

      private static class Singletons {
         private final Map<Integer, RewardProgramData.TypeEnum> intToTypeEnumMap = build();
         private static final RewardProgramData.TypeEnum.Singletons INSTANCE = new RewardProgramData.TypeEnum.Singletons();

         private static Map<Integer, RewardProgramData.TypeEnum> build() {
            Map<Integer, RewardProgramData.TypeEnum> map = new HashMap();
            EnumUtils.populateLookUpMap(map, RewardProgramData.TypeEnum.class);
            return map;
         }

         public static RewardProgramData.TypeEnum.Singletons getInstance() {
            return INSTANCE;
         }

         public Map<Integer, RewardProgramData.TypeEnum> getIntToTypeEnumMap() {
            return this.intToTypeEnumMap;
         }
      }
   }
}
