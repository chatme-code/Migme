package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedBaseTrigger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class UserRewardedTagsStateHandler extends SpecificTriggerTypeStateHandler<UserRewardedBaseTrigger> {
   private static final Logger log = Log4JUtils.getLogger(UserRewardedTagsStateHandler.class);

   public UserRewardedTagsStateHandler() {
      super(UserRewardedBaseTrigger.class);
   }

   protected RewardProgramStateHandler.PerformReturn performWithSpecificTrigger(RewardProgramData program, UserRewardedBaseTrigger trigger, String currentStateData) {
      Bag<String> expectedRewardedTags = this.extractExpectedRewardedTags(program);
      if (expectedRewardedTags.uniqueItemCount() <= 0) {
         log.error("RewardProgram id:[" + program.id + "] param:[" + "qlfydRwdPgmPrms" + "] is empty.");
         return RewardProgramStateHandler.PerformReturn.NOTHING;
      } else {
         Set<String> filteredRewardedTags = this.filterRewardedTags(expectedRewardedTags, trigger.getQualifiedUserRewardProgram());
         if (filteredRewardedTags.isEmpty()) {
            return RewardProgramStateHandler.PerformReturn.NOTHING;
         } else {
            Bag<String> rewardedTagsState = StringUtil.isBlank(currentStateData) ? new Bag() : StringBagJSONSerde.fromString(currentStateData);
            rewardedTagsState.addAll(filteredRewardedTags);
            RemainderBag<String> remainder = rewardedTagsState.matchAndFilter(expectedRewardedTags);
            return RewardProgramStateHandler.PerformReturn.saveState(remainder.isConsumed(), StringBagJSONSerde.toString(remainder));
         }
      }
   }

   private Set<String> filterRewardedTags(Bag<String> expectedRewardedTags, RewardProgramData qualifiedRewardProgramData) {
      Set<String> filteredRewardedTags = new HashSet();
      Iterator i$ = qualifiedRewardProgramData.getParameterNames().iterator();

      while(i$.hasNext()) {
         String tag = (String)i$.next();
         if (expectedRewardedTags.getCount(tag) > 0) {
            filteredRewardedTags.add(tag);
         }
      }

      return filteredRewardedTags;
   }

   private Bag<String> extractExpectedRewardedTags(RewardProgramData program) {
      List<String> expectedRewardTags = program.getStringListParam("qlfydRwdPgmPrms");
      return (new Bag()).addAll(expectedRewardTags);
   }

   public String getStateKeySuffix() {
      return "rwdedtags";
   }
}
