package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class StoreItemInventoryData implements Serializable {
   private long ID;
   private int storeItemID;
   private Integer userID;
   private StoreItemInventoryData.StoreItemInventoryLocationEnum location;
   private Date dateCreated;
   private StoreItemData storeItemData;

   public StoreItemInventoryData.StoreItemInventoryLocationEnum getLocation() {
      return this.location;
   }

   public void setLocation(StoreItemInventoryData.StoreItemInventoryLocationEnum location) {
      this.location = location;
   }

   public StoreItemData getStoreItemData() {
      return this.storeItemData;
   }

   public void setStoreItemData(StoreItemData storeItemData) {
      this.storeItemData = storeItemData;
   }

   public StoreItemInventoryData() {
   }

   public StoreItemInventoryData(ResultSet rs) throws SQLException {
      this.ID = rs.getLong("ID");
      this.storeItemID = rs.getInt("StoreItemID");
      this.userID = rs.getInt("UserID");
      this.dateCreated = rs.getTimestamp("DateCreated");
      this.location = StoreItemInventoryData.StoreItemInventoryLocationEnum.fromValue(rs.getInt("location"));
   }

   public long getID() {
      return this.ID;
   }

   public void setID(long iD) {
      this.ID = iD;
   }

   public int getStoreItemID() {
      return this.storeItemID;
   }

   public void setStoreItemID(int storeItemID) {
      this.storeItemID = storeItemID;
   }

   public Integer getUserID() {
      return this.userID;
   }

   public void setUserID(Integer userID) {
      this.userID = userID;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public static enum StoreItemInventoryLocationEnum {
      UNLOCK(1);

      private int value;

      private StoreItemInventoryLocationEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static StoreItemInventoryData.StoreItemInventoryLocationEnum fromValue(int value) {
         StoreItemInventoryData.StoreItemInventoryLocationEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            StoreItemInventoryData.StoreItemInventoryLocationEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
