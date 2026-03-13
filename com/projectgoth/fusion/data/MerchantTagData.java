package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.ejb.CreateException;

public class MerchantTagData implements Serializable {
   public Integer id;
   public Integer userID;
   public Integer merchantUserID;
   public String dateCreated;
   public String lastSalesDate;
   public MerchantTagData.StatusEnum status;
   public Double amount;
   public String currency;
   public Long accountEntryID;

   public MerchantTagData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.dateCreated = DateTimeUtils.getStringForMigcore(rs.getTimestamp("dateCreated"));
      this.lastSalesDate = DateTimeUtils.getStringForMigcore(rs.getTimestamp("lastSalesDate"));

      try {
         this.amount = rs.getDouble("amount");
         this.currency = rs.getString("currency");
      } catch (Exception var5) {
         this.amount = 0.0D;
         this.currency = null;
      }

      this.userID = rs.getInt("userID");
      if (this.userID == 0) {
         this.userID = null;
      }

      this.merchantUserID = rs.getInt("merchantUserID");
      if (this.merchantUserID == 0) {
         this.merchantUserID = null;
      }

      Number intVal = (Number)rs.getObject("status");
      if (intVal != null) {
         this.status = MerchantTagData.StatusEnum.fromValue(intVal.intValue());
      }

      try {
         this.accountEntryID = rs.getLong("accountentryid");
      } catch (Exception var4) {
         this.accountEntryID = null;
      }

   }

   public boolean isActive() {
      return this.status == MerchantTagData.StatusEnum.ACTIVE;
   }

   public Double getTransferTagAmount(String nCurrency) throws Exception {
      double transferRate = 1.0D;
      DecimalFormat df = new DecimalFormat("0.00");

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CurrencyData newCurrency = misBean.getCurrency(nCurrency);
         double minRequirementAUD = SystemProperty.getDouble("MinMerchantUserTagAmountAUD", 0.01D);
         if (this.currency == null) {
            return Double.valueOf(df.format(minRequirementAUD * newCurrency.exchangeRate));
         } else {
            CurrencyData transactionCurrency = misBean.getCurrency(this.currency);
            if (newCurrency.exchangeRate < transactionCurrency.exchangeRate) {
               transferRate = newCurrency.exchangeRate;
            } else {
               transferRate = transactionCurrency.exchangeRate;
            }

            double tagAmount = 0.0D;
            if (transferRate > 1.0D) {
               tagAmount = (this.amount / transactionCurrency.exchangeRate + minRequirementAUD) * newCurrency.exchangeRate;
            } else {
               tagAmount = (this.amount + minRequirementAUD) / transactionCurrency.exchangeRate * newCurrency.exchangeRate;
            }

            return Double.valueOf(df.format(tagAmount));
         }
      } catch (CreateException var12) {
         throw new Exception(var12.getMessage());
      }
   }

   public long getLastSalesDateInMiliSeconds() throws ParseException {
      return DateTimeUtils.getTimeInMilisecondsFromString(this.lastSalesDate);
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1),
      PENDING(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MerchantTagData.StatusEnum fromValue(int value) {
         MerchantTagData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MerchantTagData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      TOP_MERCHANT_TAG(1, SystemProperty.getInt((String)"TopMerchantTagValidPeriod", 43200), "Top Merchant Tag"),
      NON_TOP_MERCHANT_TAG(2, SystemProperty.getInt((String)"NonTopMerchantTagValidPeriod", 43200), "Non-Top Merchant Tag");

      private int value;
      private String description;
      private int validity;

      private TypeEnum(int value, int validity, String description) {
         this.value = value;
         this.validity = validity;
         this.description = description;
      }

      public int value() {
         return this.value;
      }

      public int validity() {
         return this.validity;
      }

      public String description() {
         return this.description;
      }

      public static MerchantTagData.TypeEnum fromValue(int value) {
         MerchantTagData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MerchantTagData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
