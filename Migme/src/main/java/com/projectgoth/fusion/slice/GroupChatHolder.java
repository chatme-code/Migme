package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupChatHolder {
   public GroupChat value;

   public GroupChatHolder() {
   }

   public GroupChatHolder(GroupChat value) {
      this.value = value;
   }

   public GroupChatHolder.Patcher getPatcher() {
      return new GroupChatHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupChatHolder.this.value = (GroupChat)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupChat";
      }
   }
}
