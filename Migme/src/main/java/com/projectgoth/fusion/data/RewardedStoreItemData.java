package com.projectgoth.fusion.data;

import com.projectgoth.leto.common.storeitem.StoreItem;
import com.projectgoth.leto.common.storeitem.StoreItemType;
import java.io.Serializable;

public class RewardedStoreItemData implements Serializable, StoreItem {
   private final StoreItemData storeItemData;

   public RewardedStoreItemData(StoreItemData storeItemData) {
      this.storeItemData = storeItemData;
   }

   public int getId() {
      return this.storeItemData.id;
   }

   public StoreItemData.TypeEnum getType() {
      return this.storeItemData.type;
   }

   public Integer getReferenceID() {
      return this.storeItemData.referenceID;
   }

   public String getName() {
      return this.storeItemData.name;
   }

   public double getPrice() {
      return this.storeItemData.price;
   }

   public String getCurrency() {
      return this.storeItemData.currency;
   }

   public String toString() {
      return "storeitem#" + this.getId();
   }

   public int getItemId() {
      return this.getId();
   }

   public StoreItemType getItemType() {
      return this.getType().toStoreItemType();
   }

   public String getItemName() {
      return this.getName();
   }

   public String getItemReference() {
      return this.storeItemData.referenceID.toString();
   }
}
