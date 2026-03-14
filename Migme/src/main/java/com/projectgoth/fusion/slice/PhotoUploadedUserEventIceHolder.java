package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class PhotoUploadedUserEventIceHolder {
   public PhotoUploadedUserEventIce value;

   public PhotoUploadedUserEventIceHolder() {
   }

   public PhotoUploadedUserEventIceHolder(PhotoUploadedUserEventIce value) {
      this.value = value;
   }

   public PhotoUploadedUserEventIceHolder.Patcher getPatcher() {
      return new PhotoUploadedUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            PhotoUploadedUserEventIceHolder.this.value = (PhotoUploadedUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::PhotoUploadedUserEventIce";
      }
   }
}
