package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataAccountBalance extends FusionPacket {
   public FusionPktDataAccountBalance() {
      super(PacketType.ACCOUNT_BALANCE);
   }

   public FusionPktDataAccountBalance(short transactionId) {
      super(PacketType.ACCOUNT_BALANCE, transactionId);
   }

   public FusionPktDataAccountBalance(FusionPacket packet) {
      super(packet);
   }

   public final String getAccountBalance() {
      return this.getStringField((short)1);
   }

   public final void setAccountBalance(String accountBalance) {
      this.setField((short)1, accountBalance);
   }
}
