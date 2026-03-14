package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface RewardPrograms {
   void loadPrograms();

   void add(RewardProgramData var1);

   void resetCachedProperties();

   Collection<RewardProgramData> getAll();

   RewardProgramData get(int var1);

   /** @deprecated */
   @Deprecated
   List<RewardProgramData> get(RewardProgramData.TypeEnum var1);

   RewardPrograms.RewardProgramDataList getRewardPrograms(RewardProgramData.TypeEnum var1);

   public static class Instance {
      private final RewardPrograms rewardProgramsOld = new RewardProgramsOld();
      private final RewardPrograms rewardPrograms = new RewardProgramsImpl();

      private RewardPrograms _get() {
         return (Boolean)SystemPropertyEntities.Temp.Cache.se218Enabled.getValue() ? this.rewardPrograms : this.rewardProgramsOld;
      }

      public static final RewardPrograms get() {
         return RewardPrograms.Instance.InstanceHolder.instance._get();
      }

      public static class InstanceHolder {
         public static final RewardPrograms.Instance instance = new RewardPrograms.Instance();
      }
   }

   public static class RewardProgramDataList {
      private final List<RewardProgramData> rewardPrograms;
      private final boolean needsToCheckUserReputation;

      public RewardProgramDataList(List<RewardProgramData> rewardPrograms, boolean needsToCheckUserReputation) {
         this.rewardPrograms = Collections.unmodifiableList(rewardPrograms);
         this.needsToCheckUserReputation = needsToCheckUserReputation;
      }

      public List<RewardProgramData> getRewardPrograms() {
         return this.rewardPrograms;
      }

      public boolean needsToCheckUserReputation() {
         return this.needsToCheckUserReputation;
      }
   }
}
