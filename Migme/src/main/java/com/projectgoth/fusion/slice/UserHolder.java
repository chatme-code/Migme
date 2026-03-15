package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserHolder {
   public User value;

   public UserHolder() {
   }

   public UserHolder(User value) {
      this.value = value;
   }

   public UserHolder.Patcher getPatcher() {
      return new UserHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserHolder.this.value = (User)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::User";
      }
   }
}
