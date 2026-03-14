package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SessionCacheAdminHolder {
   public SessionCacheAdmin value;

   public SessionCacheAdminHolder() {
   }

   public SessionCacheAdminHolder(SessionCacheAdmin value) {
      this.value = value;
   }

   public SessionCacheAdminHolder.Patcher getPatcher() {
      return new SessionCacheAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SessionCacheAdminHolder.this.value = (SessionCacheAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SessionCacheAdmin";
      }
   }
}
