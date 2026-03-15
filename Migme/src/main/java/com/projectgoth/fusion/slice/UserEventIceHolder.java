package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserEventIceHolder {
   public UserEventIce value;

   public UserEventIceHolder() {
   }

   public UserEventIceHolder(UserEventIce value) {
      this.value = value;
   }

   public UserEventIceHolder.Patcher getPatcher() {
      return new UserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserEventIceHolder.this.value = (UserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserEventIce";
      }
   }
}
