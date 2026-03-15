package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceData implements Serializable {
   public Integer id;
   public String name;
   public String description;
   public Integer freeTrialDays;
   public Integer durationDays;
   public Double awardedCredit;
   public String awardedCreditCurrency;
   public ServiceData.BillingMethodEnum billingMethod;
   public Double cost;
   public String costCurrency;
   public String billingConfirmationSMS;
   public String expiryReminderSMS;
   public ServiceData.StatusEnum status;

   public ServiceData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.freeTrialDays = (Integer)rs.getObject("freeTrialDays");
      this.durationDays = (Integer)rs.getObject("durationDays");
      this.awardedCredit = (Double)rs.getObject("awardedCredit");
      this.awardedCreditCurrency = rs.getString("awardedCreditCurrency");
      this.cost = (Double)rs.getObject("cost");
      this.costCurrency = rs.getString("costCurrency");
      this.billingConfirmationSMS = rs.getString("billingConfirmationSMS");
      this.expiryReminderSMS = rs.getString("expiryReminderSMS");
      Integer intVal = (Integer)rs.getObject("billingMethod");
      if (intVal != null) {
         this.billingMethod = ServiceData.BillingMethodEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = ServiceData.StatusEnum.fromValue(intVal);
      }

   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ServiceData.StatusEnum fromValue(int value) {
         ServiceData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ServiceData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum BillingMethodEnum {
      USER_ACCOUNT(1),
      PREMIUM_SMS(2);

      private int value;

      private BillingMethodEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ServiceData.BillingMethodEnum fromValue(int value) {
         ServiceData.BillingMethodEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ServiceData.BillingMethodEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
