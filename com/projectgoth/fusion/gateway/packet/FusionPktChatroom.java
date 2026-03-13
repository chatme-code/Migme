package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataChatroom;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatroom extends FusionPktDataChatroom {
   public FusionPktChatroom() {
   }

   public FusionPktChatroom(short transactionId) {
      super(transactionId);
   }

   public FusionPktChatroom(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatroom(short transactionId, ChatRoomData chatRoomData) {
      this(transactionId);
      if (chatRoomData.name != null) {
         this.setChatroomName(chatRoomData.name);
      }

      if (chatRoomData.description != null) {
         this.setDescription(chatRoomData.description);
      }

      if (chatRoomData.maximumSize != null) {
         this.setMaxmiumNumberOfParticipants(chatRoomData.maximumSize);
      }

      if (chatRoomData.size != null) {
         this.setNumberOfParticipants(chatRoomData.size);
      }

      if (chatRoomData.adultOnly != null) {
         this.setAdultOnly(chatRoomData.adultOnly);
      }

      if (chatRoomData.userOwned != null) {
         this.setUserOwned(chatRoomData.userOwned);
      }

      if (chatRoomData.groupID != null) {
         this.setGroupId(chatRoomData.groupID);
      }

      if (chatRoomData.id != null) {
         this.setChatroomId(chatRoomData.id);
      }

      if (chatRoomData.creator != null) {
         this.setCreatorName(chatRoomData.creator);
      }

      if (chatRoomData.themeID != null) {
         this.setThemeId(chatRoomData.themeID);
      }

      if (chatRoomData.newOwner != null) {
         this.setNewOwnerName(chatRoomData.newOwner);
      }

   }

   public FusionPktChatroom(short transactionId, ChatRoomData chatRoomData, short category) {
      this(transactionId, chatRoomData);
      this.setCategoryId(category);
   }
}
