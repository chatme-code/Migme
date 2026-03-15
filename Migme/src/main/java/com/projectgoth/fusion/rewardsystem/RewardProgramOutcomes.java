package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeProcessor;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeProcessorInstances;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RewardProgramOutcomes {
   public static final RewardProgramOutcomes EMPTY;
   private final List<RewardProgramOutcomeData> outcomeList;
   private final boolean templateDataRequired;

   protected RewardProgramOutcomes(List<RewardProgramOutcomeData> outcomeList, boolean requiresTemplateData) {
      this.outcomeList = outcomeList == null ? Collections.EMPTY_LIST : outcomeList;
      this.templateDataRequired = requiresTemplateData;
   }

   public List<RewardProgramOutcomeData> getOutcomeList() {
      return this.outcomeList;
   }

   public boolean isTemplateDataRequired() {
      return this.templateDataRequired;
   }

   public static RewardProgramOutcomes getOutcomes(RewardProgramOutcomeProcessorInstances outcomeProcessorInstances, RewardProgramData program, RewardProgramTrigger trigger) throws Exception {
      List<RewardProgramOutcomeProcessor> rewardOutcomeProcessors = getOutcomeProcessors(outcomeProcessorInstances, program);
      if (rewardOutcomeProcessors != null && !rewardOutcomeProcessors.isEmpty()) {
         boolean requiresTemplateData = false;
         List<RewardProgramOutcomeData> outcomeDataList = new ArrayList(rewardOutcomeProcessors.size());
         Iterator i$ = rewardOutcomeProcessors.iterator();

         while(i$.hasNext()) {
            RewardProgramOutcomeProcessor outcomeProcessor = (RewardProgramOutcomeProcessor)i$.next();
            RewardProgramOutcomeData outcomeData = outcomeProcessor.getOutcome(program, trigger);
            if (outcomeData != null) {
               outcomeDataList.add(outcomeData);
               requiresTemplateData |= outcomeData.requiresTemplateData();
            }
         }

         return new RewardProgramOutcomes(outcomeDataList, requiresTemplateData);
      } else {
         return EMPTY;
      }
   }

   private static List<RewardProgramOutcomeProcessor> getOutcomeProcessors(RewardProgramOutcomeProcessorInstances outcomeProcessorInstances, RewardProgramData programData) {
      List<String> processorClassNames = programData.getOutcomeProcessorClassNames();
      List<RewardProgramOutcomeProcessor> outcomeProcessors = new ArrayList(processorClassNames.size());
      Iterator i$ = processorClassNames.iterator();

      while(i$.hasNext()) {
         String processorClassName = (String)i$.next();

         try {
            outcomeProcessors.add(outcomeProcessorInstances.getInstance(processorClassName));
         } catch (Exception var7) {
            RewardCentre.log.error("Unable to get outcome processor :[" + processorClassName + "]  Exception:[" + var7 + "]", var7);
         }
      }

      return outcomeProcessors;
   }

   static {
      EMPTY = new RewardProgramOutcomes(Collections.EMPTY_LIST, false);
   }
}
