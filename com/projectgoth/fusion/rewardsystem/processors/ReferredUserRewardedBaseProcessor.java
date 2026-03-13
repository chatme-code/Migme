package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBException;

public abstract class ReferredUserRewardedBaseProcessor extends RewardProgramProcessor {
   @RewardProgramParamName
   public static final String MAX_REFERRED_USER_MIG_LEVEL_PARAM_KEY = "maxRefdUsrMigLvl";
   @RewardProgramParamName
   public static final String MIN_REFERRED_USER_MIG_LEVEL_PARAM_KEY = "minRefdUsrMigLvl";
   @RewardProgramParamName
   public static final String REFERRED_USER_TYPES_PARAM_KEY = "refdUsrTypes";
   @RewardProgramParamName
   public static final String REFERRED_USER_TYPES_IS_WHITELIST_PARAM_KEY = "refdUsrTypesIsWhitelist";
   @RewardProgramParamName
   public static final String MAX_REFERRED_USER_REGISTRATION_DATE_PARAM_KEY = "maxRefdUsrRegDate";
   @RewardProgramParamName
   public static final String MIN_REFERRED_USER_REGISTRATION_DATE_PARAM_KEY = "minRefdUsrRegDate";
   @RewardProgramParamName
   public static final String REFERRED_USER_COUNTRY_IDS_PARAM_KEY = "refdUsrCtryIDs";
   @RewardProgramParamName
   public static final String REFERRED_USER_COUNTRY_IDS_IS_WHITELIST_PARAM_KEY = "refdUsrCtryIDsIsWhiteLst";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_IDS_PARAM_KEY = "refdUsrRwdPgmIDs";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_TYPES_PARAM_KEY = "refdUsrRwdPgmTypes";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_PARAMS_PARAM_KEY = "refdUsrRwdPgmPrms";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_IDS_IS_WHITELIST_PARAM_KEY = "refdUsrRwdPgmIDsIsWhiteLst";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_TYPES_IS_WHITELIST_PARAM_KEY = "refdUsrRwdPgmTypesIsWhiteLst";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDPROGRAM_PARAMS_IS_WHITELIST_PARAM_KEY = "refdUsrRwdPgmPrmsIsWhiteLst";

   protected final boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) throws Exception {
      if (!(trigger instanceof ReferredUserRewardedBaseTrigger)) {
         return false;
      } else {
         ReferredUserRewardedBaseTrigger rfdUsrRewardedTrigger = (ReferredUserRewardedBaseTrigger)trigger;
         return passedCommonFilter(programData, rfdUsrRewardedTrigger) ? this.processInternal(programData, rfdUsrRewardedTrigger) : false;
      }
   }

   private static boolean passedCommonFilter(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) throws EJBException, RemoteException, CreateException {
      return passedReferredUserRegDateConstraint(programData, trigger.getReferredUserData()) && passedReferredUserCountryIDConstraint(programData, trigger.getReferredUserData()) && passedReferredUserRewardProgramConstraints(programData, trigger.getReferredUserRewardProgram()) && passedReferredUserMigLevels(programData, trigger.getReferredUserData());
   }

   private static boolean passedReferredUserCountryIDConstraint(RewardProgramData programData, UserData referredUserData) {
      String countryIDStr = referredUserData.countryID == null ? "null" : referredUserData.countryID.toString();
      return programData.matchesSetOfStringsConstraint("refdUsrCtryIDs", "refdUsrCtryIDsIsWhiteLst", countryIDStr);
   }

   private static boolean passedReferredUserRegDateConstraint(RewardProgramData programData, UserData referredUserData) {
      return programData.matchesDateConstraint("minRefdUsrRegDate", true, referredUserData.dateRegistered) && programData.matchesDateConstraint("maxRefdUsrRegDate", false, referredUserData.dateRegistered);
   }

   private static boolean passedReferredUserMigLevels(RewardProgramData programData, UserData referredUserData) throws EJBException, RemoteException, CreateException {
      if (programData.hasParameter("minRefdUsrMigLvl") || programData.hasParameter("maxRefdUsrMigLvl")) {
         int minMigLevel = programData.getIntParam("minRefdUsrMigLvl", -1);
         int maxMigLevel = programData.getIntParam("maxRefdUsrMigLvl", -1);
         if (minMigLevel != -1 || maxMigLevel != -1) {
            UserReputationScoreAndLevelData reputation = RewardCentre.getInstance().getUserReputationScoreAndLevelData(referredUserData);
            int referredUserMigLevel = reputation.level;
            return (minMigLevel == -1 || minMigLevel <= referredUserMigLevel) && (maxMigLevel == -1 || referredUserMigLevel <= maxMigLevel);
         }
      }

      return true;
   }

   private static boolean passedReferredUserRewardProgramIDsConstraints(RewardProgramData programData, RewardProgramData referredRewardProgram) {
      if (programData.hasParameter("refdUsrRwdPgmIDs")) {
         String rewardProgramIDStr = referredRewardProgram.id.toString();
         return programData.matchesSetOfStringsConstraint("refdUsrRwdPgmIDs", "refdUsrRwdPgmIDsIsWhiteLst", rewardProgramIDStr);
      } else {
         return true;
      }
   }

   private static boolean passedReferredUserRewardProgramTypesConstraints(RewardProgramData programData, RewardProgramData referredRewardProgram) {
      if (programData.hasParameter("refdUsrRwdPgmTypes")) {
         String rewardProgramTypeStr = String.valueOf(referredRewardProgram.type.value());
         return programData.matchesSetOfStringsConstraint("refdUsrRwdPgmTypes", "refdUsrRwdPgmTypesIsWhiteLst", rewardProgramTypeStr);
      } else {
         return true;
      }
   }

   private static boolean passedReferredUserRewardProgramParamsConstraints(RewardProgramData programData, RewardProgramData referredRewardProgram) {
      if (programData.hasParameter("refdUsrRwdPgmPrms")) {
         boolean isWhiteList = programData.getBoolParam("refdUsrRwdPgmPrmsIsWhiteLst", true);
         Set<String> inclusionExclusionReferredProgramParams = programData.getStringSetParam("refdUsrRwdPgmPrms");
         boolean found = false;
         Iterator i$ = inclusionExclusionReferredProgramParams.iterator();

         String param;
         do {
            if (!i$.hasNext()) {
               return isWhiteList ? found : !found;
            }

            param = (String)i$.next();
         } while(!referredRewardProgram.hasParameter(param));

         found = true;
         return true;
      } else {
         return true;
      }
   }

   private static boolean passedReferredUserRewardProgramConstraints(RewardProgramData programData, RewardProgramData referredRewardProgram) {
      return passedReferredUserRewardProgramIDsConstraints(programData, referredRewardProgram) && passedReferredUserRewardProgramTypesConstraints(programData, referredRewardProgram) && passedReferredUserRewardProgramParamsConstraints(programData, referredRewardProgram);
   }

   protected abstract boolean processInternal(RewardProgramData var1, ReferredUserRewardedBaseTrigger var2) throws Exception;
}
