package com.projectgoth.fusion.data;

import java.io.Serializable;

public class StoreItemToUnlockData implements Serializable {
   private final int storeitemid;
   private final int quantity;

   public StoreItemToUnlockData(int storeitemid, int quantity) {
      this.storeitemid = storeitemid;
      this.quantity = quantity;
   }

   public int getStoreitemID() {
      return this.storeitemid;
   }

   public int getQuantity() {
      return this.quantity;
   }
}
