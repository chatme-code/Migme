package com.projectgoth.fusion.rewardsystem.instrumentation;

public interface Sample {
   SampleCategory getSampleCategory();

   ProcessingResultEnum getProcessingResult();

   long getReceivedTimestamp();

   long getDequeuedTimestamp();

   long getEndProcessTimestamp();
}
