package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _EventStoreAdminTie extends _EventStoreAdminDisp implements TieBase {
   private _EventStoreAdminOperations _ice_delegate;

   public _EventStoreAdminTie() {
   }

   public _EventStoreAdminTie(_EventStoreAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_EventStoreAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _EventStoreAdminTie) ? false : this._ice_delegate.equals(((_EventStoreAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public EventStoreStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
