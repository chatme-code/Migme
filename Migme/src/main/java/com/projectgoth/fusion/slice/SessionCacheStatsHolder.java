package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SessionCacheStatsHolder {
   public SessionCacheStats value;

   public SessionCacheStatsHolder() {
   }

   public SessionCacheStatsHolder(SessionCacheStats value) {
      this.value = value;
   }

   public SessionCacheStatsHolder.Patcher getPatcher() {
      return new SessionCacheStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SessionCacheStatsHolder.this.value = (SessionCacheStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SessionCacheStats";
      }
   }
}
