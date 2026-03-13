package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SessionCacheHolder {
   public SessionCache value;

   public SessionCacheHolder() {
   }

   public SessionCacheHolder(SessionCache value) {
      this.value = value;
   }

   public SessionCacheHolder.Patcher getPatcher() {
      return new SessionCacheHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SessionCacheHolder.this.value = (SessionCache)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SessionCache";
      }
   }
}
