package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAccountBalance;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAccountBalance extends FusionPktDataAccountBalance {
   public FusionPktAccountBalance() {
   }

   public FusionPktAccountBalance(short transactionId) {
      super(transactionId);
   }

   public FusionPktAccountBalance(short transactionId, UserData userData, CurrencyData currencyData) {
      super(transactionId);
      this.setAccountBalance(currencyData.format(userData.balance));
   }

   public FusionPktAccountBalance(FusionPacket packet) {
      super(packet);
   }
}
