package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserWallPostUserEventIceHolder {
   public UserWallPostUserEventIce value;

   public UserWallPostUserEventIceHolder() {
   }

   public UserWallPostUserEventIceHolder(UserWallPostUserEventIce value) {
      this.value = value;
   }

   public UserWallPostUserEventIceHolder.Patcher getPatcher() {
      return new UserWallPostUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserWallPostUserEventIceHolder.this.value = (UserWallPostUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserWallPostUserEventIce";
      }
   }
}
