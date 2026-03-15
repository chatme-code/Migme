package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupUserEventIceHolder {
   public GroupUserEventIce value;

   public GroupUserEventIceHolder() {
   }

   public GroupUserEventIceHolder(GroupUserEventIce value) {
      this.value = value;
   }

   public GroupUserEventIceHolder.Patcher getPatcher() {
      return new GroupUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupUserEventIceHolder.this.value = (GroupUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupUserEventIce";
      }
   }
}
