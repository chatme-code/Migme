package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupDonationUserEventIceHolder {
   public GroupDonationUserEventIce value;

   public GroupDonationUserEventIceHolder() {
   }

   public GroupDonationUserEventIceHolder(GroupDonationUserEventIce value) {
      this.value = value;
   }

   public GroupDonationUserEventIceHolder.Patcher getPatcher() {
      return new GroupDonationUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupDonationUserEventIceHolder.this.value = (GroupDonationUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupDonationUserEventIce";
      }
   }
}
