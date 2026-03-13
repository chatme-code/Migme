package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatRoomOld extends FusionPacket {
   public FusionPktChatRoomOld() {
      super((short)701);
   }

   public FusionPktChatRoomOld(short transactionId) {
      super((short)701, transactionId);
   }

   public FusionPktChatRoomOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatRoomOld(short transactionId, ChatRoomData chatRoomData) {
      this(transactionId);
      if (chatRoomData.name != null) {
         this.setChatRoomName(chatRoomData.name);
      }

      if (chatRoomData.description != null) {
         this.setDescription(chatRoomData.description);
      }

      if (chatRoomData.maximumSize != null) {
         this.setMaximumSize(chatRoomData.maximumSize);
      }

      if (chatRoomData.size != null) {
         this.setSize(chatRoomData.size);
      }

      if (chatRoomData.adultOnly != null) {
         this.setAdultOnly((byte)(chatRoomData.adultOnly ? 1 : 0));
      }

      if (chatRoomData.userOwned != null) {
         this.setUserOwned((byte)(chatRoomData.userOwned ? 1 : 0));
      }

      if (chatRoomData.groupID != null) {
         this.setGroupId(chatRoomData.groupID);
      }

      if (chatRoomData.id != null) {
         this.setChatRoomId(chatRoomData.id);
      }

      if (chatRoomData.creator != null) {
         this.setCreator(chatRoomData.creator);
      }

      if (chatRoomData.themeID != null) {
         this.setThemeId(chatRoomData.themeID);
      }

      if (chatRoomData.newOwner != null) {
         this.setNewOwner(chatRoomData.newOwner);
      }

   }

   public FusionPktChatRoomOld(short transactionId, ChatRoomData chatRoomData, short category) {
      this(transactionId, chatRoomData);
      this.setCategory(category);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public String getDescription() {
      return this.getStringField((short)2);
   }

   public void setDescription(String description) {
      this.setField((short)2, description);
   }

   public Integer getMaximumSize() {
      return this.getIntField((short)3);
   }

   public void setMaximumSize(int maximumSize) {
      this.setField((short)3, maximumSize);
   }

   public Integer getSize() {
      return this.getIntField((short)4);
   }

   public void setSize(int size) {
      this.setField((short)4, size);
   }

   public Byte getAdultOnly() {
      return this.getByteField((short)5);
   }

   public void setAdultOnly(byte adultOnly) {
      this.setField((short)5, adultOnly);
   }

   public Short getCategory() {
      return this.getShortField((short)6);
   }

   public void setCategory(short category) {
      this.setField((short)6, category);
   }

   public Byte getUserOwned() {
      return this.getByteField((short)7);
   }

   public void setUserOwned(byte userOwned) {
      this.setField((short)7, userOwned);
   }

   public Integer getGroupId() {
      return this.getIntField((short)8);
   }

   public void setGroupId(int groupId) {
      this.setField((short)8, groupId);
   }

   public Integer getChatRoomId() {
      return this.getIntField((short)9);
   }

   public void setChatRoomId(int chatRoomId) {
      this.setField((short)9, chatRoomId);
   }

   public String getCreator() {
      return this.getStringField((short)10);
   }

   public void setCreator(String creator) {
      this.setField((short)10, creator);
   }

   public Integer getThemeId() {
      return this.getIntField((short)11);
   }

   public void setThemeId(int themeId) {
      this.setField((short)11, themeId);
   }

   public String getNewOwner() {
      return this.getStringField((short)12);
   }

   public void setNewOwner(String newOwner) {
      this.setField((short)12, newOwner);
   }
}
