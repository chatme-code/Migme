package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AddingFriendUserEventIceHolder {
   public AddingFriendUserEventIce value;

   public AddingFriendUserEventIceHolder() {
   }

   public AddingFriendUserEventIceHolder(AddingFriendUserEventIce value) {
      this.value = value;
   }

   public AddingFriendUserEventIceHolder.Patcher getPatcher() {
      return new AddingFriendUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AddingFriendUserEventIceHolder.this.value = (AddingFriendUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AddingFriendUserEventIce";
      }
   }
}
