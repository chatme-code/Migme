package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupJoinedUserEventIceHolder {
   public GroupJoinedUserEventIce value;

   public GroupJoinedUserEventIceHolder() {
   }

   public GroupJoinedUserEventIceHolder(GroupJoinedUserEventIce value) {
      this.value = value;
   }

   public GroupJoinedUserEventIceHolder.Patcher getPatcher() {
      return new GroupJoinedUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupJoinedUserEventIceHolder.this.value = (GroupJoinedUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupJoinedUserEventIce";
      }
   }
}
