package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.fdl.enums.ImDetailType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataImAvailable;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktImAvailable extends FusionPktDataImAvailable {
   public FusionPktImAvailable() {
   }

   public FusionPktImAvailable(short transactionId) {
      super(transactionId);
   }

   public FusionPktImAvailable(FusionPacket packet) {
      super(packet);
   }

   public FusionPktImAvailable(short transactionId, ImType imType, String name, MessageType messageType, ImDetailType imDetail, boolean supportsGroupChat) {
      super(transactionId);
      this.setImType(imType);
      this.setName(name);
      this.setMessageType(messageType);
      this.setImDetail(imDetail);
      this.setSupportsGroupChat(supportsGroupChat);
   }

   public FusionPktImAvailable(Enums.IMEnum imTypeEnum, ImDetailType imDetail) {
      this((short)0, imTypeEnum, imDetail);
   }

   public FusionPktImAvailable(short transactionId, Enums.IMEnum imTypeEnum, ImDetailType imDetail) {
      this(transactionId, imTypeEnum.getImType(), imTypeEnum.getName(), imTypeEnum.getMessageType(), imDetail, imTypeEnum.supportsGroupChat());
   }
}
