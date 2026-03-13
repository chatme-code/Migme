package com.projectgoth.fusion.rewardsystem.outcomes;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class RewardProgramOutcomeProcessorInstances {
   private ConcurrentHashMap<String, RewardProgramOutcomeProcessor> outcomeProcessorInstances = new ConcurrentHashMap();

   public RewardProgramOutcomeProcessor getInstance(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      RewardProgramOutcomeProcessor processor = (RewardProgramOutcomeProcessor)this.outcomeProcessorInstances.get(className);
      if (processor == null) {
         Class<RewardProgramOutcomeProcessor> processorClass = Class.forName(className);
         RewardProgramOutcomeProcessor newProcessor = (RewardProgramOutcomeProcessor)processorClass.newInstance();
         RewardProgramOutcomeProcessor oldProcessor = (RewardProgramOutcomeProcessor)this.outcomeProcessorInstances.putIfAbsent(className, newProcessor);
         processor = oldProcessor == null ? newProcessor : oldProcessor;
      }

      return processor;
   }

   public boolean canInstantiate(Logger logger, String className) {
      try {
         this.getInstance(className);
         return true;
      } catch (Exception var4) {
         logger.error("Unable to instantiate [" + className + "].Exception:[" + var4 + "]", var4);
         return false;
      }
   }
}
