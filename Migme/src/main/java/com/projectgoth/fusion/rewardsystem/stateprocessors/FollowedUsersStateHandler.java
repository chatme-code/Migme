package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.MigboFollowingEventTrigger;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class FollowedUsersStateHandler extends SpecificTriggerTypeStateHandler<MigboFollowingEventTrigger> {
   public static final String MIN_UNIQUELY_FOLLOWED_USER_COUNT = "minUniquelyFollowedCount";
   private static final String ITEM_DELIMITER = ";";
   private static final int RADIX = 36;
   private static final String BASE36_ZERO_STR = Integer.toString(0, 36);
   private static final StringUtil.ToStringConverter<Integer> USERID_TO_BASE36_CONVERTER = new StringUtil.ToStringConverter<Integer>() {
      public String convert(Integer object) {
         return object == null ? FollowedUsersStateHandler.BASE36_ZERO_STR : Integer.toString(object, 36);
      }
   };

   public FollowedUsersStateHandler() {
      super(MigboFollowingEventTrigger.class);
   }

   protected RewardProgramStateHandler.PerformReturn performWithSpecificTrigger(RewardProgramData program, MigboFollowingEventTrigger trigger, String currentStateData) {
      Set<Integer> userIDSet = new HashSet();
      if (!StringUtil.isBlank(currentStateData)) {
         parseUserIDSet(userIDSet, currentStateData);
      }

      switch(trigger.getRelationshipEvent()) {
      case MUTUALLY_FOLLOWING:
      case NEW:
         int minimumFollowedUser = this.getExpectedMinimumUniqueFollowedUserCount(program);
         int userIDCountBeforeUpdate = userIDSet.size();
         userIDSet.add(trigger.getFollowedUser().userID);
         int userIDCountAfterUpdate = userIDSet.size();
         if (userIDCountAfterUpdate == userIDCountBeforeUpdate) {
            return RewardProgramStateHandler.PerformReturn.NOTHING;
         }

         int factor = userIDCountAfterUpdate / minimumFollowedUser;
         int remainder = userIDCountAfterUpdate % minimumFollowedUser;
         return RewardProgramStateHandler.PerformReturn.saveState(factor >= 1 && remainder == 0, encodeUserIDSet(userIDSet));
      default:
         return RewardProgramStateHandler.PerformReturn.NOTHING;
      }
   }

   private int getExpectedMinimumUniqueFollowedUserCount(RewardProgramData program) {
      int val = program.getIntParam("minUniquelyFollowedCount", 1);
      if (val < 1) {
         throw new IllegalArgumentException("Program [" + program.id + "] has invalid value for [" + "minUniquelyFollowedCount" + "]");
      } else {
         return val;
      }
   }

   public static void parseUserIDSet(Set<Integer> targetSet, String rawData) {
      StringTokenizer stk = new StringTokenizer(rawData, ";");

      while(stk.hasMoreTokens()) {
         String base36Number = stk.nextToken();
         if (!StringUtil.isBlank(base36Number)) {
            Integer userId = Integer.valueOf(base36Number, 36);
            targetSet.add(userId);
         }
      }

   }

   public static String encodeUserIDSet(Set<Integer> sourceSet) {
      return StringUtil.join(sourceSet, ";", USERID_TO_BASE36_CONVERTER);
   }

   public String getStateKeySuffix() {
      return "fl";
   }
}
