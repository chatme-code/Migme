package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CollectedRewardProgramTriggerSummaryDataIceHolder {
   public CollectedRewardProgramTriggerSummaryDataIce value;

   public CollectedRewardProgramTriggerSummaryDataIceHolder() {
   }

   public CollectedRewardProgramTriggerSummaryDataIceHolder(CollectedRewardProgramTriggerSummaryDataIce value) {
      this.value = value;
   }

   public CollectedRewardProgramTriggerSummaryDataIceHolder.Patcher getPatcher() {
      return new CollectedRewardProgramTriggerSummaryDataIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CollectedRewardProgramTriggerSummaryDataIceHolder.this.value = (CollectedRewardProgramTriggerSummaryDataIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce";
      }
   }
}
