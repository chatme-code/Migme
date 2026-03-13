package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CreatedChatroomUserEventIceHolder {
   public CreatedChatroomUserEventIce value;

   public CreatedChatroomUserEventIceHolder() {
   }

   public CreatedChatroomUserEventIceHolder(CreatedChatroomUserEventIce value) {
      this.value = value;
   }

   public CreatedChatroomUserEventIceHolder.Patcher getPatcher() {
      return new CreatedChatroomUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CreatedChatroomUserEventIceHolder.this.value = (CreatedChatroomUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::CreatedChatroomUserEventIce";
      }
   }
}
