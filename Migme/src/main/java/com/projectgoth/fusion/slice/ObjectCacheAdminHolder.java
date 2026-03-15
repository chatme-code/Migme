package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ObjectCacheAdminHolder {
   public ObjectCacheAdmin value;

   public ObjectCacheAdminHolder() {
   }

   public ObjectCacheAdminHolder(ObjectCacheAdmin value) {
      this.value = value;
   }

   public ObjectCacheAdminHolder.Patcher getPatcher() {
      return new ObjectCacheAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ObjectCacheAdminHolder.this.value = (ObjectCacheAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ObjectCacheAdmin";
      }
   }
}
