package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedBaseTrigger;
import java.util.Iterator;
import java.util.Set;

public abstract class UserRewardedBaseProcessor extends RewardProgramProcessor {
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_IDS_PARAM_KEY = "qlfydRwdPgmIDs";
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_TYPES_PARAM_KEY = "qlfydRwdPgmTypes";
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_PARAMS_PARAM_KEY = "qlfydRwdPgmPrms";
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_IDS_IS_WHITELIST_PARAM_KEY = "qlfydRwdPgmIDsIsWhiteLst";
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_TYPES_IS_WHITELIST_PARAM_KEY = "qlfydRwdPgmTypesIsWhiteLst";
   @RewardProgramParamName
   public static final String QUALIFIED_REWARDPROGRAM_PARAMS_IS_WHITELIST_PARAM_KEY = "qlfydRwdPgmPrmsIsWhiteLst";

   protected final boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) throws Exception {
      if (!(trigger instanceof UserRewardedBaseTrigger)) {
         return false;
      } else {
         UserRewardedBaseTrigger usrRewardedTrigger = (UserRewardedBaseTrigger)trigger;
         return passedFulfilledUserRewardProgramConstraints(programData, usrRewardedTrigger.getQualifiedUserRewardProgram()) ? this.processInternal(programData, usrRewardedTrigger) : false;
      }
   }

   private static boolean passedQualifiedRewardProgramIDsConstraints(RewardProgramData programData, RewardProgramData qualifiedRewardProgram) {
      if (programData.hasParameter("qlfydRwdPgmIDs")) {
         String rewardProgramIDStr = qualifiedRewardProgram.id.toString();
         return programData.matchesSetOfStringsConstraint("qlfydRwdPgmIDs", "qlfydRwdPgmIDsIsWhiteLst", rewardProgramIDStr);
      } else {
         return true;
      }
   }

   private static boolean passedQualifiedRewardProgramTypesConstraints(RewardProgramData programData, RewardProgramData qualifiedRewardProgram) {
      if (programData.hasParameter("qlfydRwdPgmTypes")) {
         String rewardProgramTypeStr = String.valueOf(qualifiedRewardProgram.type.value());
         return programData.matchesSetOfStringsConstraint("qlfydRwdPgmTypes", "qlfydRwdPgmTypesIsWhiteLst", rewardProgramTypeStr);
      } else {
         return true;
      }
   }

   private static boolean passedQualifiedRewardProgramParamsConstraints(RewardProgramData programData, RewardProgramData qualifiedRewardProgram) {
      if (programData.hasParameter("qlfydRwdPgmPrms")) {
         boolean isWhiteList = programData.getBoolParam("qlfydRwdPgmPrmsIsWhiteLst", true);
         Set<String> inclusionExclusionReferredProgramParams = programData.getStringSetParam("qlfydRwdPgmPrms");
         boolean found = false;
         Iterator i$ = inclusionExclusionReferredProgramParams.iterator();

         String param;
         do {
            if (!i$.hasNext()) {
               return isWhiteList ? found : !found;
            }

            param = (String)i$.next();
         } while(!qualifiedRewardProgram.hasParameter(param));

         found = true;
         return true;
      } else {
         return true;
      }
   }

   private static boolean passedFulfilledUserRewardProgramConstraints(RewardProgramData programData, RewardProgramData qualifiedRewardProgram) {
      return passedQualifiedRewardProgramIDsConstraints(programData, qualifiedRewardProgram) && passedQualifiedRewardProgramTypesConstraints(programData, qualifiedRewardProgram) && passedQualifiedRewardProgramParamsConstraints(programData, qualifiedRewardProgram);
   }

   protected abstract boolean processInternal(RewardProgramData var1, UserRewardedBaseTrigger var2) throws Exception;
}
