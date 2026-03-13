package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ProfileUpdatedUserEventIceHolder {
   public ProfileUpdatedUserEventIce value;

   public ProfileUpdatedUserEventIceHolder() {
   }

   public ProfileUpdatedUserEventIceHolder(ProfileUpdatedUserEventIce value) {
      this.value = value;
   }

   public ProfileUpdatedUserEventIceHolder.Patcher getPatcher() {
      return new ProfileUpdatedUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ProfileUpdatedUserEventIceHolder.this.value = (ProfileUpdatedUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ProfileUpdatedUserEventIce";
      }
   }
}
