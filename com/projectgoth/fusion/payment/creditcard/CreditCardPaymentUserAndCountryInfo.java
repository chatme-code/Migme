package com.projectgoth.fusion.payment.creditcard;

import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.UserData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreditCardPaymentUserAndCountryInfo {
   public int userID;
   public String username;
   public boolean isNewUser;
   public String mobilePhone;
   public boolean mobileVerified;
   public boolean emailVerified;
   public UserData.TypeEnum userType;
   public int countryId;
   public String currency;
   public CountryData.AllowCreditCardEnum allowCreditCardPayment;
   public String isoCountryCode;
   public String isoLanguageCode;
   public Integer allowBankTransfer;
   public Integer allowWesternUnion;
   public Integer allowChequePayment;
   public String creditCardCurrency;
   public String ipAddress;

   public CreditCardPaymentUserAndCountryInfo() {
   }

   public CreditCardPaymentUserAndCountryInfo(ResultSet rs) throws SQLException {
      this.userID = rs.getInt("userid");
      this.username = rs.getString("username");
      this.isNewUser = rs.getBoolean("isnewuser");
      this.mobilePhone = rs.getString("mobilephone");
      this.mobileVerified = rs.getBoolean("mobileVerified");
      this.emailVerified = rs.getBoolean("emailVerified");
      this.userType = UserData.TypeEnum.fromValue(rs.getInt("type"));
      this.countryId = rs.getInt("id");
      this.currency = rs.getString("creditcardcurrency");
      this.allowCreditCardPayment = CountryData.AllowCreditCardEnum.fromValue(rs.getInt("allowcreditcard"));
      this.isoCountryCode = rs.getString("isocountrycode");
      this.isoLanguageCode = rs.getString("isolanguagecode");
      this.allowBankTransfer = rs.getInt("AllowBankTransfer");
      this.allowWesternUnion = rs.getInt("AllowWesternUnion");
      this.allowChequePayment = rs.getInt("AllowChequePayment");
      this.creditCardCurrency = rs.getString("creditcardcurrency");
   }
}
