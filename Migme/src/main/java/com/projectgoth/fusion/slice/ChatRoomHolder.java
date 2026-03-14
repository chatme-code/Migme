package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ChatRoomHolder {
   public ChatRoom value;

   public ChatRoomHolder() {
   }

   public ChatRoomHolder(ChatRoom value) {
      this.value = value;
   }

   public ChatRoomHolder.Patcher getPatcher() {
      return new ChatRoomHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ChatRoomHolder.this.value = (ChatRoom)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ChatRoom";
      }
   }
}
