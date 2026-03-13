package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import javax.ejb.CreateException;

public class FusionPktTransferCredit extends FusionRequest {
   public FusionPktTransferCredit() {
      super((short)909);
   }

   public FusionPktTransferCredit(short transactionId) {
      super((short)909, transactionId);
   }

   public FusionPktTransferCredit(FusionPacket packet) {
      super(packet);
   }

   public String getUsername() {
      return this.getStringField((short)1);
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public Integer getAmount() {
      return this.getIntField((short)2);
   }

   public void setAmount(int amount) {
      this.setField((short)2, amount);
   }

   public Byte getCommit() {
      return this.getByteField((short)3);
   }

   public void setCommit(byte commit) {
      this.setField((short)3, commit);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         String transferer = connection.getUsername();
         String transferee = this.getUsername();
         if (transferee == null) {
            throw new Exception("Transferee not specified");
         } else {
            Integer intVar = this.getAmount();
            if (intVar == null) {
               throw new Exception("Transfer amount not specified");
            } else {
               Byte commit = this.getCommit();
               if (commit == null) {
                  commit = 2;
               }

               double amount = (double)intVar / 100.0D;
               Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               String infoText;
               if (commit == 2) {
                  CreditTransferData creditData = accountEJB.transferCredit(transferer, transferee, amount, false, (String)null, new AccountEntrySourceData(connection));
                  AccountEntryData accountEntry = creditData.getAccountEntryData();
                  AccountBalanceData accountBalance = creditData.getAccountBalanceData();

                  try {
                     DecimalFormat df = new DecimalFormat("0.00");
                     infoText = misEJB.getInfoText(18);
                     infoText = infoText.replaceAll("%t", df.format(-accountEntry.amount) + " " + accountEntry.currency).replaceAll("%u", transferee).replaceAll("%b", accountBalance.format());
                  } catch (Exception var16) {
                     infoText = "You have successfully transferred credit to " + transferee;
                  }
               } else {
                  try {
                     CurrencyData userCurrency = accountEJB.getUsersLocalCurrency(transferer);
                     infoText = misEJB.getInfoText(23);
                     infoText = infoText.replaceAll("%t", userCurrency.format(amount)).replaceAll("%u", transferee);
                  } catch (Exception var15) {
                     infoText = "You are about to transfer credit to " + transferee;
                  }
               }

               FusionPktOk pkt = new FusionPktOk(this.transactionId);
               pkt.setServerResponse(infoText);
               return new FusionPacket[]{pkt};
            }
         }
      } catch (CreateException var17) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Transfer credit failed - Failed to create AccountEJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var18) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Transfer credit failed - " + RMIExceptionHelper.getRootMessage(var18));
         return new FusionPacket[]{pktError};
      } catch (Exception var19) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Transfer credit failed - " + var19.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
