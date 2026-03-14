package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CashReceiptData implements Serializable {
   public Integer id;
   public Date dateCreated;
   public String enteredBy;
   public Date dateReceived;
   public Double amountSent;
   public Double amountReceived;
   public Double amountCredited;
   public String matchedBy;
   public Date dateMatched;
   public String senderUsername;
   public String providerTransactionID;
   public String paymentDetails;
   public String comments;
   public CashReceiptData.StatusEnum status;
   public CashReceiptData.TypeEnum type;
   public Integer referenceCashReceiptId;
   public Double bonus;
   public String mobilePhone;

   public CashReceiptData() {
   }

   public CashReceiptData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.dateCreated = rs.getTimestamp("datecreated");
      this.dateReceived = rs.getTimestamp("datereceived");
      this.enteredBy = rs.getString("enteredby");
      this.amountSent = rs.getDouble("amountsent");
      this.amountReceived = rs.getDouble("amountreceived");
      this.amountCredited = rs.getDouble("amountcredited");
      this.type = CashReceiptData.TypeEnum.fromValue(rs.getInt("type"));
      this.matchedBy = rs.getString("matchedby");
      this.dateMatched = rs.getTimestamp("datematched");
      this.senderUsername = rs.getString("senderusername");
      this.status = CashReceiptData.StatusEnum.fromValue(rs.getInt("status"));
      this.providerTransactionID = rs.getString("ProviderTransactionID");
      this.paymentDetails = rs.getString("PaymentDetails");
      this.comments = rs.getString("comments");
      this.mobilePhone = rs.getString("mobilePhone");
      this.referenceCashReceiptId = rs.getInt("referenceCashReceiptId");
      this.bonus = rs.getDouble("bonus");
   }

   public String toString() {
      return "CashReceiptData [id=" + this.id + ", dateCreated=" + this.dateCreated + ", enteredBy=" + this.enteredBy + ", dateReceived=" + this.dateReceived + ", amountSent=" + this.amountSent + ", amountReceived=" + this.amountReceived + ", amountCredited=" + this.amountCredited + ", matchedBy=" + this.matchedBy + ", dateMatched=" + this.dateMatched + ", senderUsername=" + this.senderUsername + ", providerTransactionID=" + this.providerTransactionID + ", paymentDetails=" + this.paymentDetails + ", comments=" + this.comments + ", status=" + this.status + ", type=" + this.type + ", referenceCashReceiptId=" + this.referenceCashReceiptId + ", bonus=" + this.bonus + ", mobilePhone=" + this.mobilePhone + "]";
   }

   public static enum TypeEnum {
      TELEGRAPHIC_TRANSFER(0),
      DIRECT_CREDIT(1),
      WESTERN_UNION(1);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CashReceiptData.TypeEnum fromValue(int value) {
         CashReceiptData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CashReceiptData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      UNMATCHED(0),
      MATCHED(1),
      DELETED(2),
      REVERSED(3);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CashReceiptData.StatusEnum fromValue(int value) {
         CashReceiptData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CashReceiptData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
