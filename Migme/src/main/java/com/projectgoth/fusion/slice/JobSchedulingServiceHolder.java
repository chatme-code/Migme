package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class JobSchedulingServiceHolder {
   public JobSchedulingService value;

   public JobSchedulingServiceHolder() {
   }

   public JobSchedulingServiceHolder(JobSchedulingService value) {
      this.value = value;
   }

   public JobSchedulingServiceHolder.Patcher getPatcher() {
      return new JobSchedulingServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            JobSchedulingServiceHolder.this.value = (JobSchedulingService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::JobSchedulingService";
      }
   }
}
