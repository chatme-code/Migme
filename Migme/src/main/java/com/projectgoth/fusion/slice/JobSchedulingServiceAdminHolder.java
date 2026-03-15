package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class JobSchedulingServiceAdminHolder {
   public JobSchedulingServiceAdmin value;

   public JobSchedulingServiceAdminHolder() {
   }

   public JobSchedulingServiceAdminHolder(JobSchedulingServiceAdmin value) {
      this.value = value;
   }

   public JobSchedulingServiceAdminHolder.Patcher getPatcher() {
      return new JobSchedulingServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            JobSchedulingServiceAdminHolder.this.value = (JobSchedulingServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::JobSchedulingServiceAdmin";
      }
   }
}
