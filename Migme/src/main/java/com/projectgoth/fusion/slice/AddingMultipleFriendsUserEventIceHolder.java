package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AddingMultipleFriendsUserEventIceHolder {
   public AddingMultipleFriendsUserEventIce value;

   public AddingMultipleFriendsUserEventIceHolder() {
   }

   public AddingMultipleFriendsUserEventIceHolder(AddingMultipleFriendsUserEventIce value) {
      this.value = value;
   }

   public AddingMultipleFriendsUserEventIceHolder.Patcher getPatcher() {
      return new AddingMultipleFriendsUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AddingMultipleFriendsUserEventIceHolder.this.value = (AddingMultipleFriendsUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AddingMultipleFriendsUserEventIce";
      }
   }
}
