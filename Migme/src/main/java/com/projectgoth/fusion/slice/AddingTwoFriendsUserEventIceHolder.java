package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AddingTwoFriendsUserEventIceHolder {
   public AddingTwoFriendsUserEventIce value;

   public AddingTwoFriendsUserEventIceHolder() {
   }

   public AddingTwoFriendsUserEventIceHolder(AddingTwoFriendsUserEventIce value) {
      this.value = value;
   }

   public AddingTwoFriendsUserEventIceHolder.Patcher getPatcher() {
      return new AddingTwoFriendsUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AddingTwoFriendsUserEventIceHolder.this.value = (AddingTwoFriendsUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AddingTwoFriendsUserEventIce";
      }
   }
}
