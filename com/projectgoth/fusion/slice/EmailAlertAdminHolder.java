package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EmailAlertAdminHolder {
   public EmailAlertAdmin value;

   public EmailAlertAdminHolder() {
   }

   public EmailAlertAdminHolder(EmailAlertAdmin value) {
      this.value = value;
   }

   public EmailAlertAdminHolder.Patcher getPatcher() {
      return new EmailAlertAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EmailAlertAdminHolder.this.value = (EmailAlertAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EmailAlertAdmin";
      }
   }
}
