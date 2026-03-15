package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupUserPostUserEventIceHolder {
   public GroupUserPostUserEventIce value;

   public GroupUserPostUserEventIceHolder() {
   }

   public GroupUserPostUserEventIceHolder(GroupUserPostUserEventIce value) {
      this.value = value;
   }

   public GroupUserPostUserEventIceHolder.Patcher getPatcher() {
      return new GroupUserPostUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupUserPostUserEventIceHolder.this.value = (GroupUserPostUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupUserPostUserEventIce";
      }
   }
}
