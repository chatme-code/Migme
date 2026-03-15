package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class PotData implements Serializable {
   private Integer id;
   private Integer botID;
   private String botInstanceID;
   private Date dateCreated;
   private Date datePaidOut;
   private Double rakeAmount;
   private Double rakeFundedAmount;
   private Double rakePercent;
   private PotData.StatusEnum status;

   public PotData() {
   }

   public PotData(int botID, String botInstanceID, double rakePercent) {
      this.botID = botID;
      this.botInstanceID = botInstanceID;
      this.dateCreated = new Date();
      this.rakePercent = rakePercent;
      this.status = PotData.StatusEnum.ACTIVE;
   }

   public static PotData fromResultSet(ResultSet rs) throws SQLException {
      PotData potData = new PotData();
      potData.id = rs.getInt("id");
      potData.botID = rs.getInt("botid");
      potData.botInstanceID = rs.getString("botinstanceid");
      potData.dateCreated = rs.getDate("datecreated");
      potData.datePaidOut = rs.getDate("datepaidout");
      potData.rakeAmount = rs.getDouble("rakeamount");
      potData.rakeFundedAmount = rs.getDouble("rakefundedamount");
      potData.rakePercent = rs.getDouble("rakepercent");
      potData.status = PotData.StatusEnum.fromValue(rs.getInt("status"));
      return potData;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Integer getBotID() {
      return this.botID;
   }

   public void setBotID(Integer botID) {
      this.botID = botID;
   }

   public String getBotInstanceID() {
      return this.botInstanceID;
   }

   public void setBotInstanceID(String botInstanceID) {
      this.botInstanceID = botInstanceID;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public Date getDatePaidOut() {
      return this.datePaidOut;
   }

   public void setDatePaidOut(Date datePaidOut) {
      this.datePaidOut = datePaidOut;
   }

   public Double getRakeAmount() {
      return this.rakeAmount;
   }

   public void setRakeAmount(Double rakeAmount) {
      this.rakeAmount = rakeAmount;
   }

   public Double getRakeFundedAmount() {
      return this.rakeFundedAmount;
   }

   public void setRakeFundedAmount(Double rakeFundedAmount) {
      this.rakeFundedAmount = rakeFundedAmount;
   }

   public Double getRakePercent() {
      return this.rakePercent;
   }

   public void setRakePercent(Double rakePercent) {
      this.rakePercent = rakePercent;
   }

   public PotData.StatusEnum getStatus() {
      return this.status;
   }

   public void setStatus(PotData.StatusEnum status) {
      this.status = status;
   }

   public static enum StatusEnum {
      ACTIVE(1),
      PAID_OUT(2),
      CANCELED(3);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static PotData.StatusEnum fromValue(int value) {
         PotData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            PotData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
