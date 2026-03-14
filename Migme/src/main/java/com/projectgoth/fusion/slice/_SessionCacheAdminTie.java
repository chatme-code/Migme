package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _SessionCacheAdminTie extends _SessionCacheAdminDisp implements TieBase {
   private _SessionCacheAdminOperations _ice_delegate;

   public _SessionCacheAdminTie() {
   }

   public _SessionCacheAdminTie(_SessionCacheAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_SessionCacheAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _SessionCacheAdminTie) ? false : this._ice_delegate.equals(((_SessionCacheAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public SessionCacheStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
