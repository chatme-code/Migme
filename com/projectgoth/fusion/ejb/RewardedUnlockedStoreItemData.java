package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.leto.common.storeitem.StoreItem;
import com.projectgoth.leto.common.storeitem.StoreItemType;
import java.io.Serializable;

public class RewardedUnlockedStoreItemData implements Serializable, StoreItem {
   private final int quantity;
   private final StoreItemData storeItemData;

   public RewardedUnlockedStoreItemData(StoreItemData storeItemData, int quantity) {
      this.quantity = quantity;
      this.storeItemData = storeItemData;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public StoreItemData getStoreItemData() {
      return this.storeItemData;
   }

   public int getItemId() {
      return this.storeItemData.id;
   }

   public String getItemName() {
      return this.storeItemData.name;
   }

   public StoreItemType getItemType() {
      return this.storeItemData.type.toStoreItemType();
   }

   public String getItemReference() {
      return this.storeItemData.referenceID.toString();
   }

   public double getPrice() {
      return this.storeItemData.price;
   }

   public String getCurrency() {
      return this.storeItemData.currency;
   }
}
