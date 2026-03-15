package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class VirtualGiftReceivedData implements Serializable {
   private Integer id;
   private String username;
   private Date dateCreated;
   private VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation;
   private Integer virtualGiftID;
   private String sender;
   private boolean privateGift;
   private boolean removed;
   private String message;
   private String name;
   private String location;
   private Integer storeItemID;
   private StoreItemData storeItemData;

   public StoreItemData getStoreItemData() {
      return this.storeItemData;
   }

   public void setStoreItemData(StoreItemData storeItemData) {
      this.storeItemData = storeItemData;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Integer getStoreItemID() {
      return this.storeItemID;
   }

   public void setStoreItemID(Integer storeItemID) {
      this.storeItemID = storeItemID;
   }

   public String toString() {
      return this.id.toString();
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public VirtualGiftReceivedData.PurchaseLocationEnum getPurchaseLocation() {
      return this.purchaseLocation;
   }

   public void setPurchastLocation(VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation) {
      this.purchaseLocation = purchaseLocation;
   }

   public Integer getVirtualGiftID() {
      return this.virtualGiftID;
   }

   public void setVirtualGiftID(Integer virtualGiftID) {
      this.virtualGiftID = virtualGiftID;
   }

   public String getSender() {
      return this.sender;
   }

   public void setSender(String sender) {
      this.sender = sender;
   }

   public boolean isPrivateGift() {
      return this.privateGift;
   }

   public void setPrivateGift(boolean privateGift) {
      this.privateGift = privateGift;
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public void setRemoved(boolean removed) {
      this.removed = removed;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public VirtualGiftReceivedData() {
   }

   public VirtualGiftReceivedData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.virtualGiftID = rs.getInt("giftid");
      this.sender = rs.getString("sender");
      this.username = rs.getString("username");
      this.dateCreated = rs.getTimestamp("datecreated");
      this.message = rs.getString("message");
      this.removed = rs.getBoolean("removed");
      this.privateGift = rs.getBoolean("private");
   }

   public String getLocation() {
      return this.location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public static enum PurchaseLocationEnum {
      PROFILE(1),
      PRIVATE_CHAT_COMMAND(2),
      GROUP_CHAT_COMMAND(3),
      CHATROOM_COMMAND(4),
      PRIVATE_CHAT_MENU(5),
      GROUP_CHAT_MENU(6),
      CHATROOM_MENU(7),
      FRIENDS_LIST(8),
      STORE(9),
      GIFT_VIEW(10),
      MARKETING_REWARD(11);

      private int value;

      private PurchaseLocationEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static VirtualGiftReceivedData.PurchaseLocationEnum fromValue(int value) {
         VirtualGiftReceivedData.PurchaseLocationEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            VirtualGiftReceivedData.PurchaseLocationEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
