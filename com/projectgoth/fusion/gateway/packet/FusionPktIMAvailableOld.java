package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktIMAvailableOld extends FusionPacket {
   public FusionPktIMAvailableOld() {
      super((short)208);
   }

   public FusionPktIMAvailableOld(short transactionId) {
      super((short)208, transactionId);
   }

   public FusionPktIMAvailableOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktIMAvailableOld(byte imType, String name, byte messageType, byte imDetail, byte supportsGroupChat) {
      this((short)0, imType, name, messageType, imDetail, supportsGroupChat);
   }

   public FusionPktIMAvailableOld(short transactionId, byte imType, String name, byte messageType, byte imDetail, byte supportsGroupChat) {
      super((short)208, transactionId);
      this.setIMType(imType);
      this.setName(name);
      this.setMessageType(messageType);
      this.setIMDetail(imDetail);
      this.setSupportsGroupChat(supportsGroupChat);
   }

   public FusionPktIMAvailableOld(Enums.IMEnum imTypeEnum, byte imDetail) {
      this((short)0, imTypeEnum, imDetail);
   }

   public FusionPktIMAvailableOld(short transactionId, Enums.IMEnum imTypeEnum, byte imDetail) {
      this(transactionId, imTypeEnum.getImType().value(), imTypeEnum.getName(), imTypeEnum.getMessageType().value(), imDetail, (byte)(imTypeEnum.supportsGroupChat() ? 1 : 0));
   }

   public Byte getIMType() {
      return this.getByteField((short)1);
   }

   public void setIMType(byte imType) {
      this.setField((short)1, imType);
   }

   public String getName() {
      return this.getStringField((short)2);
   }

   public void setName(String name) {
      this.setField((short)2, name);
   }

   public Byte getMessageType() {
      return this.getByteField((short)3);
   }

   public void setMessageType(byte messageType) {
      this.setField((short)3, messageType);
   }

   public Byte getIMDetail() {
      return this.getByteField((short)4);
   }

   public void setIMDetail(byte imDetail) {
      this.setField((short)4, imDetail);
   }

   public Byte getSupportsGroupChat() {
      return this.getByteField((short)5);
   }

   public void setSupportsGroupChat(byte supportsGroupChat) {
      this.setField((short)5, supportsGroupChat);
   }
}
