package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGroupChatUserStatus extends FusionPacket {
   private byte USERTYPE_MUTED = 2;

   public FusionPktGroupChatUserStatus() {
      super((short)756);
   }

   public FusionPktGroupChatUserStatus(short transactionId) {
      super((short)756, transactionId);
   }

   public FusionPktGroupChatUserStatus(FusionPacket packet) {
      super(packet);
   }

   public Byte getUserStatusType() {
      return this.getByteField((short)1);
   }

   public void setUserStatusType(byte type) {
      this.setField((short)1, type);
   }

   public String getChatRoomName() {
      return this.getStringField((short)2);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)2, chatRoomName);
   }

   public String getFusionUserName() {
      return this.getStringField((short)3);
   }

   public void setFusionUserName(String fusionUserName) {
      this.setField((short)3, fusionUserName);
   }

   public Byte getUserType() {
      return this.getByteField((short)4);
   }

   public void isMuted(boolean isMuted) {
      Byte userType = this.getUserType();
      if (userType == null) {
         userType = 0;
      }

      if (isMuted) {
         userType = (byte)(userType | this.USERTYPE_MUTED);
      } else {
         userType = (byte)(userType & ~this.USERTYPE_MUTED);
      }

      this.setField((short)4, userType);
   }

   public static enum UserStatusTypeEnum {
      JOIN((byte)1),
      LEFT((byte)2);

      private byte value;

      private UserStatusTypeEnum(byte value) {
         this.value = value;
      }

      public byte value() {
         return this.value;
      }

      public static FusionPktGroupChatUserStatus.UserStatusTypeEnum fromValue(byte value) {
         FusionPktGroupChatUserStatus.UserStatusTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktGroupChatUserStatus.UserStatusTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
