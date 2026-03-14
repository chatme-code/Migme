package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountryData implements Serializable {
   public Integer id;
   public String name;
   public Integer iddCode;
   public String currency;
   public String creditCardCurrency;
   public String bankTransferCurrency;
   public String westernUnionCurrency;
   public String chequePaymentCurrency;
   public String webMoneyCurrency;
   public Double tax;
   public Double activationCredit;
   public Double referralCredit;
   public Double minBalanceAfterTransfer;
   public Double smsCost;
   public Double callRate;
   public Double callSignallingFee;
   public Double smsWholesaleCost;
   public Double callWholesaleRate;
   public Double premiumSMSAmount;
   public Double premiumSMSFee;
   public Double mobileRate;
   public Double mobileSignallingFee;
   public Double callThroughRate;
   public Double callThroughSignallingFee;
   public Integer userBonusProgramID;
   public Integer merchantBonusProgramID;
   public CountryData.AllowCreditCardEnum allowCreditCard;
   public Integer allowBankTransfer;
   public Integer allowWesternUnion;
   public Integer allowChequePayment;
   public Integer allowWebMoney;
   public Boolean allowPhoneCall;
   public Boolean allowEmail;
   public Boolean allowUserTransferToOtherCountry;
   public Double latitude;
   public Double longitude;
   public Integer population;
   public String isoCountryCode;
   public String isoLanguageCode;
   public Boolean allowZeroAfterIddCode;
   public Boolean lowASRDestination;
   public Integer callRetries;
   public Double smsLookoutCost;
   public Double smsBuzzCost;
   public Double smsEmailAlertCost;
   public Double minNonTopMerchantTagAmount;

   public CountryData() {
   }

   public CountryData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("ID");
      this.name = rs.getString("Name");
      this.iddCode = rs.getInt("IDDCode");
      this.currency = rs.getString("Currency");
      this.creditCardCurrency = rs.getString("CreditCardCurrency");
      this.bankTransferCurrency = rs.getString("BankTransferCurrency");
      this.westernUnionCurrency = rs.getString("WesternUnionCurrency");
      this.chequePaymentCurrency = rs.getString("ChequePaymentCurrency");
      this.webMoneyCurrency = rs.getString("WebMoneyCurrency");
      this.tax = rs.getDouble("Tax");
      this.activationCredit = rs.getDouble("ActivationCredit");
      this.referralCredit = rs.getDouble("ReferralCredit");
      this.minBalanceAfterTransfer = rs.getDouble("MinBalanceAfterTransfer");
      this.userBonusProgramID = rs.getInt("UserBonusProgramID") == 0 ? null : rs.getInt("UserBonusProgramID");
      this.merchantBonusProgramID = rs.getInt("MerchantBonusProgramID") == 0 ? null : rs.getInt("MerchantBonusProgramID");
      this.smsCost = rs.getDouble("SMSCost");
      this.callRate = rs.getDouble("CallRate");
      this.callSignallingFee = rs.getDouble("CallSignallingFee");
      this.smsWholesaleCost = rs.getDouble("SMSWholesaleCost");
      this.callWholesaleRate = rs.getDouble("CallWholesaleRate");
      this.premiumSMSAmount = rs.getDouble("PremiumSMSAmount");
      this.premiumSMSFee = rs.getDouble("PremiumSMSFee");
      this.mobileRate = rs.getDouble("MobileRate");
      this.mobileSignallingFee = rs.getDouble("MobileSignallingFee");
      this.callThroughRate = rs.getDouble("CallThroughRate");
      this.callThroughSignallingFee = rs.getDouble("CallThroughSignallingFee");
      this.latitude = rs.getDouble("Latitude");
      this.longitude = rs.getDouble("Longitude");
      this.population = rs.getInt("Population");
      this.allowCreditCard = CountryData.AllowCreditCardEnum.fromValue(rs.getInt("AllowCreditCard"));
      this.allowBankTransfer = rs.getInt("AllowBankTransfer");
      this.allowWesternUnion = rs.getInt("AllowWesternUnion");
      this.allowChequePayment = rs.getInt("AllowChequePayment");
      this.allowWebMoney = rs.getInt("AllowWebMoney");
      this.allowPhoneCall = rs.getInt("AllowPhoneCall") == 1;
      this.allowEmail = rs.getInt("AllowEmail") == 1;
      this.allowUserTransferToOtherCountry = rs.getInt("AllowUserTransferToOtherCountry") == 1;
      this.isoCountryCode = rs.getString("ISOCountryCode");
      this.isoLanguageCode = rs.getString("IsoLanguageCode");
      this.allowZeroAfterIddCode = rs.getBoolean("AllowZeroAfterIDDCode");
      this.lowASRDestination = rs.getInt("LowASRDestination") == 1;
      this.callRetries = rs.getInt("CallRetries");
      this.smsLookoutCost = rs.getDouble("SMSLookoutCost");
      this.smsBuzzCost = rs.getDouble("SMSBuzzCost");
      this.smsEmailAlertCost = rs.getDouble("SMSEmailAlertCost");
      this.minNonTopMerchantTagAmount = rs.getDouble("MinNonTopMerchantTagAmount");
   }

   public static enum AllowCreditCardEnum {
      NOT_ALLOW(0),
      ALLOW(1),
      ALLOW_IF_BIN_CHECK(2);

      private int value;

      private AllowCreditCardEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CountryData.AllowCreditCardEnum fromValue(int value) {
         CountryData.AllowCreditCardEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CountryData.AllowCreditCardEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
