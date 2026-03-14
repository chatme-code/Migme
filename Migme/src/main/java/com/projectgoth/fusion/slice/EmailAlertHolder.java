package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EmailAlertHolder {
   public EmailAlert value;

   public EmailAlertHolder() {
   }

   public EmailAlertHolder(EmailAlert value) {
      this.value = value;
   }

   public EmailAlertHolder.Patcher getPatcher() {
      return new EmailAlertHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EmailAlertHolder.this.value = (EmailAlert)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EmailAlert";
      }
   }
}
