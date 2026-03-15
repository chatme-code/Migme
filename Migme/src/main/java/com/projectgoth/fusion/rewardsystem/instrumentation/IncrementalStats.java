package com.projectgoth.fusion.rewardsystem.instrumentation;

import java.io.Serializable;

public class IncrementalStats implements Serializable {
   private long sampleCount = 0L;
   private double currentMean;
   private double currentVariance;
   private long minSample = 0L;
   private long maxSample = 0L;

   public void append(long sample) {
      long oldCount = this.sampleCount;
      double oldVariance = this.currentVariance;
      double oldMean = this.currentMean;
      ++this.sampleCount;
      if (this.sampleCount >= 2L) {
         double sampleDeltaFromOldMean = (double)sample - oldMean;
         this.currentVariance = (double)(oldCount - 1L) * oldVariance / (double)oldCount + sampleDeltaFromOldMean * sampleDeltaFromOldMean / (double)this.sampleCount;
         this.currentMean = oldMean + ((double)sample - oldMean) / (double)this.sampleCount;
         if (this.minSample > sample) {
            this.minSample = sample;
         }

         if (this.maxSample < sample) {
            this.maxSample = sample;
         }
      } else {
         this.currentVariance = 0.0D;
         this.currentMean = (double)sample;
         this.minSample = sample;
         this.maxSample = sample;
      }

   }

   public double getSampleMean() {
      return this.currentMean;
   }

   public double getSampleVariance() {
      return this.currentVariance;
   }

   public long getSampleCount() {
      return this.sampleCount;
   }

   public long getMaxSample() {
      return this.maxSample;
   }

   public long getMinSample() {
      return this.minSample;
   }
}
