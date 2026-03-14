package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAccountBalanceOld extends FusionPacket {
   public FusionPktAccountBalanceOld() {
      super((short)902);
   }

   public FusionPktAccountBalanceOld(short transactionId) {
      super((short)902, transactionId);
   }

   public FusionPktAccountBalanceOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktAccountBalanceOld(short transactionId, UserData userData, CurrencyData currencyData) {
      super((short)902, transactionId);
      this.setAccountBalance(currencyData.format(userData.balance));
   }

   public String getAccountBalance() {
      return this.getStringField((short)1);
   }

   public void setAccountBalance(String accountBalance) {
      this.setField((short)1, accountBalance);
   }
}
