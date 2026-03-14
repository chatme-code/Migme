package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ImageServerStatsHolder {
   public ImageServerStats value;

   public ImageServerStatsHolder() {
   }

   public ImageServerStatsHolder(ImageServerStats value) {
      this.value = value;
   }

   public ImageServerStatsHolder.Patcher getPatcher() {
      return new ImageServerStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ImageServerStatsHolder.this.value = (ImageServerStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ImageServerStats";
      }
   }
}
