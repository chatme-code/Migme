package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.springframework.util.StringUtils;

public class SubscriptionData implements Serializable {
   public Integer id;
   public String username;
   public Integer serviceID;
   public Date dateCreated;
   public SubscriptionData.TypeEnum type;
   public String ipAddress;
   public String mobilePhone;
   public Date expiryDate;
   public Boolean expiryReminderSent;
   public Date cancellationDate;
   public Integer billingAttempts;
   public Date lastBillingAttempt;
   public SubscriptionData.StatusEnum status;
   public String serviceName;

   public SubscriptionData() {
   }

   public SubscriptionData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.serviceID = (Integer)rs.getObject("serviceID");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.ipAddress = rs.getString("ipAddress");
      this.mobilePhone = rs.getString("mobilePhone");
      this.expiryDate = rs.getTimestamp("expiryDate");
      this.cancellationDate = rs.getTimestamp("cancellationDate");
      this.billingAttempts = (Integer)rs.getObject("billingAttempts");
      this.lastBillingAttempt = rs.getTimestamp("lastBillingAttempt");
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = SubscriptionData.TypeEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("expiryReminderSent");
      if (intVal != null) {
         this.expiryReminderSent = intVal == 1;
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = SubscriptionData.StatusEnum.fromValue(intVal);
      }

      try {
         String serviceName = rs.getString("servicename");
         if (StringUtils.hasLength(serviceName)) {
            this.serviceName = serviceName;
         }
      } catch (SQLException var4) {
      }

   }

   public static enum StatusEnum {
      PENDING(0),
      ACTIVE(1),
      CANCELLED(2),
      EXPIRED(3),
      FAILED(4);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SubscriptionData.StatusEnum fromValue(int value) {
         SubscriptionData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SubscriptionData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      FREE_TRIAL(1),
      PAID(2);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SubscriptionData.TypeEnum fromValue(int value) {
         SubscriptionData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SubscriptionData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
