package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.Voucher;
import com.projectgoth.fusion.interfaces.VoucherHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import javax.ejb.CreateException;

public class FusionPktRecharge extends FusionRequest {
   public FusionPktRecharge() {
      super((short)903);
   }

   public FusionPktRecharge(short transactionId) {
      super((short)903, transactionId);
   }

   public FusionPktRecharge(FusionPacket packet) {
      super(packet);
   }

   public String getUsername() {
      return this.getStringField((short)1);
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public Byte getRechargeMethod() {
      return this.getByteField((short)2);
   }

   public void setRechargeMethod(byte rechargeMethod) {
      this.setField((short)2, rechargeMethod);
   }

   public Byte getCardType() {
      return this.getByteField((short)3);
   }

   public void setCardType(byte cardType) {
      this.setField((short)3, cardType);
   }

   public String getCardNumber() {
      return this.getStringField((short)4);
   }

   public void setCardNumber(String cardNumber) {
      this.setField((short)4, cardNumber);
   }

   public Integer getCardPIN() {
      return this.getIntField((short)5);
   }

   public void setCardPIN(int cardPIN) {
      this.setField((short)5, cardPIN);
   }

   public String getCardExpiryDate() {
      return this.getStringField((short)6);
   }

   public void setCardExpiryDate(String cardExpiryDate) {
      this.setField((short)6, cardExpiryDate);
   }

   public String getCardHolder() {
      return this.getStringField((short)7);
   }

   public void setCardHolder(String cardHolder) {
      this.setField((short)7, cardHolder);
   }

   public String getReceiptNumber() {
      return this.getStringField((short)8);
   }

   public void setReceiptNumber(String receiptNumber) {
      this.setField((short)8, receiptNumber);
   }

   public String getRechargeCode() {
      return this.getStringField((short)9);
   }

   public void setRechargeCode(String rechargeCode) {
      this.setField((short)9, rechargeCode);
   }

   public Integer getAmount() {
      return this.getIntField((short)10);
   }

   public void setAmount(int amount) {
      this.setField((short)10, amount);
   }

   public boolean sessionRequired() {
      return true;
   }

   private String creditCardRecharge(AccountEntrySourceData accountEntrySourceData) throws Exception {
      Integer intVal = this.getAmount();
      if (intVal == null) {
         throw new Exception("Recharge amount must be specified");
      } else {
         double amount = intVal.doubleValue() / 100.0D;
         if (amount <= 0.0D) {
            throw new Exception("Recharge amount must be greater than 0");
         } else {
            MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
            String returnText = misEJB.getInfoText(10);
            CreditCardPaymentData paymentData = new CreditCardPaymentData();
            paymentData.username = this.getUsername();
            paymentData.source = CreditCardPaymentData.SourceEnum.MIDLET;
            paymentData.ipAddress = accountEntrySourceData.ipAddress;
            paymentData.cardNumber = this.getCardNumber();
            Byte byteVal = this.getCardType();
            if (byteVal == null) {
               if (paymentData.cardNumber.startsWith("4")) {
                  paymentData.cardType = CreditCardPaymentData.CardTypeEnum.VISA;
               } else if (!paymentData.cardNumber.startsWith("51") && !paymentData.cardNumber.startsWith("52") && !paymentData.cardNumber.startsWith("53") && !paymentData.cardNumber.startsWith("54") && !paymentData.cardNumber.startsWith("55")) {
                  if (!paymentData.cardNumber.startsWith("34") && !paymentData.cardNumber.startsWith("37")) {
                     if (!paymentData.cardNumber.startsWith("6011") && !paymentData.cardNumber.startsWith("65")) {
                        if (paymentData.cardNumber.startsWith("36") || paymentData.cardNumber.startsWith("55")) {
                           paymentData.cardType = CreditCardPaymentData.CardTypeEnum.DINERS_CLUB;
                        }
                     } else {
                        paymentData.cardType = CreditCardPaymentData.CardTypeEnum.DISCOVER;
                     }
                  } else {
                     paymentData.cardType = CreditCardPaymentData.CardTypeEnum.AMEX;
                  }
               } else {
                  paymentData.cardType = CreditCardPaymentData.CardTypeEnum.MASTERCARD;
               }
            } else {
               paymentData.cardType = CreditCardPaymentData.CardTypeEnum.fromValue(byteVal.intValue());
            }

            paymentData.cardHolder = this.getCardHolder();
            paymentData.cardExpiryDate = this.getCardExpiryDate();
            intVal = this.getCardPIN();
            if (intVal != null) {
               paymentData.cardVerificationNumber = (new DecimalFormat("000")).format(intVal);
            }

            paymentData.amount = amount;
            paymentData.currency = CurrencyData.baseCurrency;
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            paymentData = accountEJB.creditCardPayment(paymentData, accountEntrySourceData);
            if (paymentData.status != CreditCardPaymentData.StatusEnum.APPROVED) {
               throw new Exception(paymentData.responseCode);
            } else {
               try {
                  AccountBalanceData accountBalance = accountEJB.getAccountBalance(this.getUsername());
                  return returnText.replaceAll("%r", accountBalance.currency.formatBaseCurrency(amount)).replaceAll("%b", accountBalance.format());
               } catch (Exception var11) {
                  return "You have successfully recharged your account";
               }
            }
         }
      }
   }

   private String redeemVoucher(AccountEntrySourceData accountEntrySourceData) throws Exception {
      MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
      String returnText = misEJB.getInfoText(27);
      Voucher voucherEJB = (Voucher)EJBHomeCache.getObject("ejb/Voucher", VoucherHome.class);
      VoucherData voucherData = voucherEJB.redeemVoucher(this.getUsername(), this.getRechargeCode(), accountEntrySourceData);

      try {
         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountBalanceData accountBalance = accountEJB.getAccountBalance(this.getUsername());
         return returnText.replaceAll("%r", (new DecimalFormat("0.00 ")).format(voucherData.amount) + voucherData.currency).replaceAll("%b", accountBalance.format());
      } catch (Exception var8) {
         return "You have successfully redeemed your voucher";
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      this.setUsername(connection.getUsername());
      return this.processRequest(new AccountEntrySourceData(connection));
   }

   public FusionPacket[] processRequest(AccountEntrySourceData accountEntrySourceData) {
      FusionPktError pktError;
      try {
         Byte method = this.getRechargeMethod();
         if (method == null) {
            throw new Exception("Recharge method not specified");
         } else {
            String returnText;
            switch(method.intValue()) {
            case 3:
               returnText = this.creditCardRecharge(accountEntrySourceData);
               break;
            case 4:
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               returnText = misEJB.getInfoText(12);
               break;
            case 5:
               returnText = this.redeemVoucher(accountEntrySourceData);
               break;
            default:
               throw new Exception("Unknown recharge method");
            }

            FusionPktOk pkt = new FusionPktOk(this.transactionId);
            pkt.setServerResponse(returnText);
            return new FusionPacket[]{pkt};
         }
      } catch (CreateException var5) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Recharge failed - Failed to create EJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var6) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Recharge failed - " + RMIExceptionHelper.getRootMessage(var6));
         return new FusionPacket[]{pktError};
      } catch (Exception var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Recharge failed - " + var7.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
