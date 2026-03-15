package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ObjectCacheHolder {
   public ObjectCache value;

   public ObjectCacheHolder() {
   }

   public ObjectCacheHolder(ObjectCache value) {
      this.value = value;
   }

   public ObjectCacheHolder.Patcher getPatcher() {
      return new ObjectCacheHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ObjectCacheHolder.this.value = (ObjectCache)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ObjectCache";
      }
   }
}
