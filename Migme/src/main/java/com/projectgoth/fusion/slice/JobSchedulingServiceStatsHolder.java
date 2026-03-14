package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class JobSchedulingServiceStatsHolder {
   public JobSchedulingServiceStats value;

   public JobSchedulingServiceStatsHolder() {
   }

   public JobSchedulingServiceStatsHolder(JobSchedulingServiceStats value) {
      this.value = value;
   }

   public JobSchedulingServiceStatsHolder.Patcher getPatcher() {
      return new JobSchedulingServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            JobSchedulingServiceStatsHolder.this.value = (JobSchedulingServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::JobSchedulingServiceStats";
      }
   }
}
