package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MigboEnums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MigboPostCreatedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class MigboPostCreatedRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MigboPostCreatedRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String ORIGINALITY_PARAM_KEY = "originality";
   @RewardProgramParamName
   public static final String HASHTAG_PARAM_KEY = "hashtag";
   @RewardProgramParamName
   public static final String APPLICATION_PARAM_KEY = "application";
   @RewardProgramParamName
   public static final String TYPE_PARAM_KEY = "type";
   @RewardProgramParamName
   public static final String PARENT_POSTID_PARAM_KEY = "parentPostID";
   @RewardProgramParamName
   public static final String SHARE_THIRD_PARTY_KEY = "shareToThirdParty";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof MigboPostCreatedTrigger)) {
         return false;
      } else {
         MigboPostCreatedTrigger bgwTrigger = (MigboPostCreatedTrigger)trigger;
         MigboEnums.MigboPostOriginalityEnum originality = MigboEnums.MigboPostOriginalityEnum.fromType(programData.getIntParam("originality", -1));
         String hashtag = programData.getStringParam("hashtag", "");
         MigboEnums.PostApplicationEnum application = MigboEnums.PostApplicationEnum.fromValue(programData.getIntParam("application", 0));
         MigboEnums.MigboPostTypeEnum postType = MigboEnums.MigboPostTypeEnum.fromValue(programData.getIntParam("type", -1));
         Enums.ThirdPartyEnum shareToThirdParty = Enums.ThirdPartyEnum.fromValue(programData.getIntParam("shareToThirdParty", -1));
         String parentPostID = programData.getStringParam("parentPostID", "");
         if (log.isDebugEnabled()) {
            log.debug("userID[" + trigger.userData.userID + "]  originality in Trigger[" + bgwTrigger.postOriginality + "] required[" + originality + "][" + programData.getIntParam("originality", -1) + "]");
         }

         boolean result = true;
         if (originality != null) {
            result &= originality == bgwTrigger.postOriginality;
         }

         if (!StringUtil.isBlank(hashtag)) {
            if (bgwTrigger.hashtags != null) {
               boolean containsRequiredHashtag = false;
               Iterator i$ = bgwTrigger.hashtags.iterator();

               while(i$.hasNext()) {
                  String s = (String)i$.next();
                  if (s.equalsIgnoreCase(hashtag)) {
                     containsRequiredHashtag = true;
                     break;
                  }
               }

               result &= containsRequiredHashtag;
            } else {
               result = false;
            }
         }

         if (application != null) {
            result &= application == bgwTrigger.application;
         }

         if (postType != null) {
            result &= postType == bgwTrigger.postType;
         }

         if (shareToThirdParty != null) {
            result &= bgwTrigger.shareToThirdParty.contains(shareToThirdParty);
         }

         if (!StringUtil.isBlank(parentPostID)) {
            result &= bgwTrigger.parentPostID != null && bgwTrigger.parentPostID.equals(parentPostID);
         }

         return result;
      }
   }
}
