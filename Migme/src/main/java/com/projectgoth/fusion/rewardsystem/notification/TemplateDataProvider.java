package com.projectgoth.fusion.rewardsystem.notification;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDataProvider {
   public static TemplateDataProvider getDefaultInstance() {
      return TemplateDataProvider.Singletons.getInstance().getTemplateDataProvider();
   }

   protected TemplateDataProvider() {
   }

   public final Map<String, String> getTemplateData(RewardProgramData rewardProgram, RewardProgramTrigger trigger, List<RewardProgramOutcomeData> outcomeList) {
      Map<String, String> templateDataMap = new HashMap();
      rewardProgram.populateTemplateDataMap(templateDataMap);
      trigger.populateTemplateDataMap(templateDataMap);
      String outcomesSizeStr;
      if (outcomeList != null) {
         for(int i = 0; i < outcomeList.size(); ++i) {
            ((RewardProgramOutcomeData)outcomeList.get(i)).populateTemplateDataMap(i, templateDataMap);
         }

         outcomesSizeStr = String.valueOf(outcomeList.size());
      } else {
         outcomesSizeStr = "0";
      }

      templateDataMap.put("outcomes.size", outcomesSizeStr);
      String defaultHostSite = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL);
      if (!StringUtil.isBlank(defaultHostSite)) {
         templateDataMap.put("sysprop.mig33_web_base_url", defaultHostSite);
      }

      this.fillTemplateData(templateDataMap, rewardProgram, trigger, outcomeList);
      return templateDataMap;
   }

   protected void fillTemplateData(Map<String, String> templateDataMap, RewardProgramData rewardProgram, RewardProgramTrigger trigger, List<RewardProgramOutcomeData> outcomeList) {
   }

   private static class Singletons {
      private final TemplateDataProvider templateDataProvider = new TemplateDataProvider();
      private static final TemplateDataProvider.Singletons INSTANCE = new TemplateDataProvider.Singletons();

      public static TemplateDataProvider.Singletons getInstance() {
         return INSTANCE;
      }

      public TemplateDataProvider getTemplateDataProvider() {
         return this.templateDataProvider;
      }
   }
}
