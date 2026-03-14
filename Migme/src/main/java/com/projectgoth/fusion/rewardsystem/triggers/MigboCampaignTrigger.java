package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class MigboCampaignTrigger extends RewardProgramTrigger {
   public static final int TAG_VALUE_EMPTY = -1;
   public int campaignID;
   public long eventTimestamp;
   public String entityType = "";
   public String entityId = "";
   public int tagValue = -1;
   public MigboCampaignTrigger.EventTypeEnum eventType;

   public MigboCampaignTrigger(MigboCampaignTrigger.EventTypeEnum eventType, UserData userData, int campaignID, long eventTimestamp, String entityType, String entityId, int tagValue) {
      super(RewardProgramData.TypeEnum.MIGBO_CAMPAIGN_EVENT, userData);
      this.eventType = eventType;
      this.eventTimestamp = eventTimestamp;
      this.entityType = entityType;
      this.tagValue = tagValue;
      this.entityId = entityId;
      this.campaignID = campaignID;
      super.quantityDelta = 1;
   }

   public static MigboCampaignTrigger getCampaignRegistrationTrigger(UserData userData, int campaignID, long eventTimestamp) {
      return new MigboCampaignTrigger(MigboCampaignTrigger.EventTypeEnum.REGISTRATION, userData, campaignID, eventTimestamp, "", "", -1);
   }

   public static MigboCampaignTrigger getTagCreatedTrigger(UserData userData, int campaignID, long eventTimestamp, String entityType, String entityId, int tagValue) {
      return new MigboCampaignTrigger(MigboCampaignTrigger.EventTypeEnum.TAG_CREATED, userData, campaignID, eventTimestamp, entityType, entityId, tagValue);
   }

   public static enum EventTypeEnum {
      REGISTRATION(1),
      TAG_CREATED(2);

      private int type;

      private EventTypeEnum(int type) {
         this.type = type;
      }

      public int intValue() {
         return this.type;
      }

      public static boolean isValid(int type) {
         return fromIntValue(type) != null;
      }

      public static MigboCampaignTrigger.EventTypeEnum fromIntValue(int type) {
         MigboCampaignTrigger.EventTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MigboCampaignTrigger.EventTypeEnum e = arr$[i$];
            if (e.type == type) {
               return e;
            }
         }

         return null;
      }

      public static MigboCampaignTrigger.EventTypeEnum fromString(String typeStr) {
         if (StringUtil.isBlank(typeStr)) {
            return null;
         } else {
            MigboCampaignTrigger.EventTypeEnum[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               MigboCampaignTrigger.EventTypeEnum e = arr$[i$];
               if (e.toString().toLowerCase().equals(typeStr.toLowerCase())) {
                  return e;
               }
            }

            return null;
         }
      }
   }
}
