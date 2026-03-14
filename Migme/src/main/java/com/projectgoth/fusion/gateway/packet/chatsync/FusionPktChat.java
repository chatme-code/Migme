package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChat extends FusionPacket {
   public FusionPktChat() {
      super((short)560);
   }

   public FusionPktChat(short transactionId) {
      super((short)560, transactionId);
   }

   public FusionPktChat(FusionPacket packet) {
      super(packet);
   }

   public String getChatIdentifier() {
      return this.getStringField((short)1);
   }

   public void setChatIdentifier(String id) {
      this.setField((short)1, id);
   }

   public String getChatDisplayName() {
      return this.getStringField((short)2);
   }

   public void setChatDisplayName(String name) {
      this.setField((short)2, name);
   }

   public Byte getChatType() {
      return this.getByteField((short)3);
   }

   public void setChatType(byte type) {
      this.setField((short)3, type);
   }

   public Integer getUnreadMessageCount() {
      return this.getIntField((short)4);
   }

   public void setUnreadMessageCount(int count) {
      this.setField((short)4, count);
   }

   public Integer getContactId() {
      return this.getIntField((short)5);
   }

   public void setContactId(int id) {
      this.setField((short)5, id);
   }

   public String getGroupOwner() {
      return this.getStringField((short)6);
   }

   public void setGroupOwner(String owner) {
      this.setField((short)6, owner);
   }

   public Byte getIsClosedChat() {
      return this.getByteField((short)7);
   }

   public void setIsClosedChat(byte closed) {
      this.setField((short)7, closed);
   }

   public String getDisplayGUID() {
      return this.getStringField((short)8);
   }

   public void setDisplayGUID(String guid) {
      this.setField((short)8, guid);
   }

   public Byte getMessageType() {
      return this.getByteField((short)9);
   }

   public void setMessageType(byte type) {
      this.setField((short)9, type);
   }

   public Long getTimestamp() {
      return this.getLongField((short)10);
   }

   public void setTimestamp(long ts) {
      this.setField((short)10, ts);
   }

   public Integer getChatListVersion() {
      return this.getIntField((short)11);
   }

   public void setChatListVersion(int ver) {
      this.setField((short)11, ver);
   }

   public Long getChatListTimestamp() {
      return this.getLongField((short)12);
   }

   public void setChatListTimestamp(long ts) {
      this.setField((short)12, ts);
   }

   public Byte getIsRenamedChat() {
      return this.getByteField((short)13);
   }

   public void setIsRenamedChat(byte renamed) {
      this.setField((short)13, renamed);
   }

   public Byte getIsPassivatedChat() {
      return this.getByteField((short)14);
   }

   public void setIsPassivatedChat(byte passivated) {
      this.setField((short)14, passivated);
   }
}
