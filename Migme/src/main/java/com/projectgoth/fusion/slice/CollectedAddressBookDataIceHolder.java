package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CollectedAddressBookDataIceHolder {
   public CollectedAddressBookDataIce value;

   public CollectedAddressBookDataIceHolder() {
   }

   public CollectedAddressBookDataIceHolder(CollectedAddressBookDataIce value) {
      this.value = value;
   }

   public CollectedAddressBookDataIceHolder.Patcher getPatcher() {
      return new CollectedAddressBookDataIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CollectedAddressBookDataIceHolder.this.value = (CollectedAddressBookDataIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::CollectedAddressBookDataIce";
      }
   }
}
