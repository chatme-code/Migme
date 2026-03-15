package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _AuthenticationServiceAdminTie extends _AuthenticationServiceAdminDisp implements TieBase {
   private _AuthenticationServiceAdminOperations _ice_delegate;

   public _AuthenticationServiceAdminTie() {
   }

   public _AuthenticationServiceAdminTie(_AuthenticationServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_AuthenticationServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _AuthenticationServiceAdminTie) ? false : this._ice_delegate.equals(((_AuthenticationServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public AuthenticationServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
