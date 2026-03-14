package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ImageServerAdminHolder {
   public ImageServerAdmin value;

   public ImageServerAdminHolder() {
   }

   public ImageServerAdminHolder(ImageServerAdmin value) {
      this.value = value;
   }

   public ImageServerAdminHolder.Patcher getPatcher() {
      return new ImageServerAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ImageServerAdminHolder.this.value = (ImageServerAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ImageServerAdmin";
      }
   }
}
