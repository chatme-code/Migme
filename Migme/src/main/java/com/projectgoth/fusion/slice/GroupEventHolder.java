package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupEventHolder {
   public GroupEvent value;

   public GroupEventHolder() {
   }

   public GroupEventHolder(GroupEvent value) {
      this.value = value;
   }

   public GroupEventHolder.Patcher getPatcher() {
      return new GroupEventHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupEventHolder.this.value = (GroupEvent)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupEvent";
      }
   }
}
