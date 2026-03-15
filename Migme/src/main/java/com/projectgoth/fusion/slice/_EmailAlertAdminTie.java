package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _EmailAlertAdminTie extends _EmailAlertAdminDisp implements TieBase {
   private _EmailAlertAdminOperations _ice_delegate;

   public _EmailAlertAdminTie() {
   }

   public _EmailAlertAdminTie(_EmailAlertAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_EmailAlertAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _EmailAlertAdminTie) ? false : this._ice_delegate.equals(((_EmailAlertAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public EmailAlertStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
