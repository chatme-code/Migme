package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _SessionCacheTie extends _SessionCacheDisp implements TieBase {
   private _SessionCacheOperations _ice_delegate;

   public _SessionCacheTie() {
   }

   public _SessionCacheTie(_SessionCacheOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_SessionCacheOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _SessionCacheTie) ? false : this._ice_delegate.equals(((_SessionCacheTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void logSession(SessionIce session, SessionMetricsIce sessionMetrics, Current __current) {
      this._ice_delegate.logSession(session, sessionMetrics, __current);
   }
}
