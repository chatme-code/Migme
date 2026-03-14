package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;

public abstract class RewardProgramStateHandler {
   public abstract RewardProgramStateHandler.PerformReturn perform(RewardProgramData var1, RewardProgramTrigger var2, String var3);

   public abstract String getStateKeySuffix();

   public static class PerformReturn {
      public final boolean save;
      public final boolean cont;
      public final String newState;
      public static final RewardProgramStateHandler.PerformReturn NOTHING = new RewardProgramStateHandler.PerformReturn(false, false, (String)null);

      private PerformReturn(boolean save, boolean cont, String newState) {
         this.save = save;
         this.cont = cont;
         this.newState = newState;
      }

      public static RewardProgramStateHandler.PerformReturn saveState(boolean cont, String state) {
         return new RewardProgramStateHandler.PerformReturn(true, cont, state);
      }
   }
}
